[CmdletBinding()]
param(
    [Parameter(Mandatory = $true)]
    [ValidateNotNullOrEmpty()]
    [string]$PythonExecutable,

    [Parameter(Mandatory = $true)]
    [ValidateNotNullOrEmpty()]
    [string]$RenderTool
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

function Resolve-ExplicitExecutable {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Path,

        [Parameter(Mandatory = $true)]
        [string]$Role
    )

    if (-not [System.IO.Path]::IsPathRooted($Path)) {
        throw "$Role must be an explicit absolute executable path."
    }

    $resolved = (Resolve-Path -LiteralPath $Path -ErrorAction Stop).Path
    if (-not (Test-Path -LiteralPath $resolved -PathType Leaf)) {
        throw "$Role executable does not exist: $resolved"
    }

    return $resolved
}

function Invoke-Checked {
    param(
        [Parameter(Mandatory = $true)]
        [string]$FilePath,

        [Parameter(Mandatory = $true)]
        [string[]]$Arguments
    )

    & $FilePath @Arguments
    if ($LASTEXITCODE -ne 0) {
        throw "Command failed with exit code ${LASTEXITCODE}: $FilePath"
    }
}

$repoRoot = [System.IO.Path]::GetFullPath((Join-Path $PSScriptRoot "..\.."))
$bootstrapPython = Resolve-ExplicitExecutable -Path $PythonExecutable -Role "Python"
$pdftoppm = Resolve-ExplicitExecutable -Path $RenderTool -Role "Poppler pdftoppm"
$pythonVersionText = & $bootstrapPython -c "import sys; print('.'.join(map(str, sys.version_info[:3])))"
if ($LASTEXITCODE -ne 0) {
    throw "Unable to query the supplied Python runtime."
}
$pythonVersion = [System.Version]$pythonVersionText.Trim()
if ($pythonVersion -lt [System.Version]"3.10") {
    throw "Python 3.10 or newer is required; received $pythonVersion."
}
$tempRoot = [System.IO.Path]::GetFullPath([System.IO.Path]::GetTempPath())
$replayRoot = [System.IO.Path]::GetFullPath(
    (Join-Path $tempRoot ("atstudio-client-pdf-" + [System.Guid]::NewGuid().ToString("N")))
)

if (-not $replayRoot.StartsWith($tempRoot, [System.StringComparison]::OrdinalIgnoreCase)) {
    throw "Replay directory escaped the operating-system temporary directory."
}

$venvRoot = Join-Path $replayRoot "venv"
$venvPython = Join-Path $venvRoot "Scripts\python.exe"
$renderDirectory = Join-Path $replayRoot "render"
$renderPrefix = Join-Path $renderDirectory "page"

try {
    New-Item -ItemType Directory -Path $renderDirectory -Force | Out-Null
    Push-Location $repoRoot
    try {
        Invoke-Checked -FilePath $bootstrapPython -Arguments @("-m", "venv", $venvRoot)
        Invoke-Checked -FilePath $venvPython -Arguments @(
            "-m",
            "pip",
            "install",
            "--disable-pip-version-check",
            "--requirement",
            (Join-Path $repoRoot "scripts\docs\client-testing-pdf-requirements.txt")
        )
        Invoke-Checked -FilePath $venvPython -Arguments @(
            "scripts/docs/generate_client_testing_pdf.py",
            "--render-tool",
            $pdftoppm
        )
        Invoke-Checked -FilePath $pdftoppm -Arguments @(
            "-png",
            "-r",
            "144",
            "output/pdf/atstudio-client-testing-guide.pdf",
            $renderPrefix
        )
        Invoke-Checked -FilePath $venvPython -Arguments @(
            "scripts/docs/verify_client_testing_pdf.py"
        )

        $renderedPages = @(
            Get-ChildItem -LiteralPath $renderDirectory -Filter "page-*.png" -File
        ).Count
        if ($renderedPages -eq 0) {
            throw "Poppler did not produce rendered PDF pages."
        }

        Write-Output "RENDERED_PAGES=$renderedPages"
        Write-Output "REPLAY=PASS"
    }
    finally {
        Pop-Location
    }
}
finally {
    if (
        (Test-Path -LiteralPath $replayRoot) -and
        $replayRoot.StartsWith($tempRoot, [System.StringComparison]::OrdinalIgnoreCase)
    ) {
        Remove-Item -LiteralPath $replayRoot -Recurse -Force
    }
}

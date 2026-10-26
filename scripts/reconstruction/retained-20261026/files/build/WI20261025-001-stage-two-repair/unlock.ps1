# User-visible native key entry only. Never capture password or ssh-add output.
$ErrorActionPreference = 'Stop'
$key = Join-Path $env:USERPROFILE '.ssh/atstudio-iwinv/id_ed25519'
if (-not (Test-Path -LiteralPath $key -PathType Leaf)) { throw 'EXISTING_KEY_MISSING' }
$agent = Get-Service ssh-agent
if ($agent.Status -ne 'Stopped' -or $agent.StartType -ne 'Disabled') { throw 'AGENT_BASELINE_CHANGED' }
Start-Process powershell.exe -Verb RunAs -WindowStyle Hidden -Wait -ArgumentList '-NoProfile -Command "Set-Service ssh-agent -StartupType Manual; Start-Service ssh-agent"'
if ((Get-Service ssh-agent).Status -ne 'Running') { throw 'AGENT_START_NOT_CONFIRMED' }
& ssh-add.exe $key
if ($LASTEXITCODE -ne 0) { throw 'KEY_UNLOCK_FAILED' }
Write-Host 'KEY READY'
Read-Host 'Press Enter to close'

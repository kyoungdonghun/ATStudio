"""Non-operational host adapter marker. Replace only under exact-target approval."""

import json


if __name__ == "__main__":
    print(json.dumps({"configured": False, "code": "HOST_HOOK_NOT_CONFIGURED"}))
    raise SystemExit(2)

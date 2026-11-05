"""WI009 independent attempt/cumulative-evidence reuse rejection, no network."""
from datetime import datetime, timezone
from pathlib import Path
import tempfile
import time
import unittest
from unittest.mock import patch

import backup_protocol as protocol
import online_capture as online
import test_online_capture as fixtures


class IndependentCostProofTests(unittest.TestCase):
    def setUp(self):
        temp = tempfile.TemporaryDirectory(prefix="ats-online-cost-")
        self.addCleanup(temp.cleanup)
        self.root = Path(temp.name)
        self.now = time.time()
        self.run = "1" * 32
        self.prior_sha = "2" * 64
        self.config = fixtures.config_fixture()
        self.config["online"] = {"machine_sha256": "3" * 64, "boot_sha256": "4" * 64, "proofs": {}}
        self.stamp = lambda value: datetime.fromtimestamp(value, timezone.utc).isoformat()
        for kind in ("target", "repository_acl", "recovery_access", "cost"):
            proof = {"version": 1, "kind": kind, "scope": "ONE_ONLINE_CAPTURE",
                     "subject_sha256": online.config_subject(self.config), "repository_id": self.config["repository_id"],
                     "machine_sha256": "3" * 64, "boot_sha256": "4" * 64, "source_evidence_sha256": "5" * 64,
                     "observed_at": self.stamp(self.now - 2), "expires_at": self.stamp(self.now + 3600)}
            if kind == "target":
                proof.update({key: True for key in ("app_tuple_matched", "sole_app_writer", "all_media_mutations_fenced",
                    "schema_release_settings_rotation_fenced", "no_uncoordinated_file_writers", "no_overlapping_deployment")})
            elif kind == "repository_acl": proof["access"] = "PRIVATE_VERIFIED"
            elif kind == "recovery_access": proof.update(operation="EXACT_SNAPSHOT_READ_VERIFIED", snapshot_id=fixtures.SNAPSHOT)
            else:
                limits = self.config["limits"]
                proof.update(run_id=self.run, prior_state_sha256=self.prior_sha,
                    upper_bound_krw=limits["projected_cost_krw"], approved_limit_krw=limits["cost_limit_krw"],
                    unknown_components=[], new_bytes_bound=sum(limits[k] for k in ("source_bytes", "dump_bytes", "metadata_bytes")),
                    retrieval_bytes_bound=limits["retrieval_bytes"])
            path = self.root / (kind + ".json")
            raw = protocol.encoded(proof)
            path.write_bytes(raw)
            self.config["online"]["proofs"][kind] = {"path": str(path), "sha256": protocol.digest(raw)}
        gate = patch.object(online, "checked_path", side_effect=lambda value, **kwargs: Path(value))
        gate.start()
        self.addCleanup(gate.stop)

    def test_same_cost_proof_cannot_authorize_a_different_run(self):
        online.verify_proofs(self.config, self.now, self.run, self.prior_sha, self.stamp(self.now - 3))
        with self.assertRaisesRegex(protocol.GateError, "COST_ATTEMPT_BINDING"):
            online.verify_proofs(self.config, self.now, "6" * 32, self.prior_sha, self.stamp(self.now - 3))

    def test_changed_predecessor_invalidates_previous_cost_evidence(self):
        with self.assertRaisesRegex(protocol.GateError, "COST_ATTEMPT_BINDING"):
            online.verify_proofs(self.config, self.now, self.run, "7" * 64, self.stamp(self.now - 3))

    def test_even_valid_time_window_cannot_reuse_evidence_observed_before_last_attempt(self):
        with self.assertRaisesRegex(protocol.GateError, "COST_PREDECESSOR_STALE"):
            online.verify_proofs(self.config, self.now, self.run, self.prior_sha, self.stamp(self.now - 1))


if __name__ == "__main__": unittest.main()

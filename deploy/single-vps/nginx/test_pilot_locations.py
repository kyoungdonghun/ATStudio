import re
import unittest
from pathlib import Path


class PilotLocationsTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        template = Path(__file__).with_name("listening-pilot-locations.conf.template").read_text()
        cls.locations = []
        for kind, path, body in re.findall(r"^location ([~=]) (\S+) \{(.*?)^}", template, re.M | re.S):
            restriction = re.search(r"limit_except ([A-Z ]+) \{ deny all; }", body)
            if not restriction:
                raise AssertionError(f"Missing method restriction: {path}")
            cls.locations.append((kind, path, restriction[1], body))

    def matching(self, path):
        return [(methods, body) for kind, pattern, methods, body in self.locations
                if (path == pattern if kind == "=" else re.fullmatch(pattern, path))]

    def test_personal_pages_and_api_contract(self):
        cases = {
            "/likes": "GET", "/playlists": "GET", "/playlists/12": "GET",
            "/playlists/12/edit": "GET", "/downloads": "GET", "/licenses/10": "GET",
            "/api/likes": "GET", "/api/likes/3": "POST DELETE",
            "/api/playlists/capacity": "GET", "/api/playlists": "GET POST",
            "/api/playlists/1": "GET PUT DELETE", "/api/playlists/1/tracks": "POST PUT",
            "/api/playlists/1/tracks/2": "DELETE",
            "/api/downloads/history": "GET", "/api/licenses/me": "GET",
            "/api/tracks/1/download": "GET", "/api/utils/download-count": "GET",
        }
        for path, expected in cases.items():
            with self.subTest(path=path):
                matches = self.matching(path)
                self.assertEqual(len(matches), 1)
                methods, body = matches[0]
                self.assertEqual(set(methods.split()), set(expected.split()))
                if path.startswith("/api/"):
                    self.assertIn("include /etc/atstudio/nginx/proxy.conf;", body)
                else:
                    self.assertIn("try_files /index.html =404;", body)
                if path in {"/api/playlists", "/api/playlists/1"}:
                    self.assertIn("client_max_body_size 32k;", body)

    def test_larger_sales_surfaces_stay_outside_pilot(self):
        for path in ("/likes/albums", "/api/likes/albums", "/api/likes/albums/11",
                     "/api/user-subscriptions/me", "/api/playlists/all", "/api/playlists/0",
                     "/api/playlists/1/tracks/batch",
                     "/playlists/public", "/register", "/api/payments", "/admin/playlists"):
            with self.subTest(path=path):
                self.assertFalse(self.matching(path))


if __name__ == "__main__":
    unittest.main()

package com.interswitchng.smartpos.shared.services

import com.interswitchng.smartpos.shared.interfaces.library.IUserService

internal class UserService: IUserService {

    override fun getToken(): String = """eyJhbGciOiJSUzI1NiJ9.eyJhdWQiOlsiaXN3LWNvbGxlY3Rpb25zIiwiaXN3LXBheW1lbnRnYXRld2F5IiwicGFzc3BvcnQiLCJwcm9qZWN0LXgtbWVyY2hhbnQiLCJ2YXVsdCJdLCJtZXJjaGFudF9jb2RlIjoiTVg2MDcyIiwicmVxdWVzdG9yX2lkIjoiMTIzODA4NTk1MDMiLCJzY29wZSI6WyJwcm9maWxlIl0sImp0aSI6IjRjMGQ4MzA5LWVkNzYtNDdlNS04YzEwLWRlMjg1MTg2NjRiZSIsInBheWFibGVfaWQiOiIzMzU5NyIsImNsaWVudF9pZCI6IklLSUFCMjNBNEUyNzU2NjA1QzFBQkMzM0NFM0MyODdFMjcyNjdGNjYwRDYxIn0.fJtfQd6RMs9JjVioPqdiY6Ei7ZAcvjhJl06jgVXW81CeZF5Yu2MCfHYAjb2NZa4xzgRPvQAvszfdMTnvennaMsI7Ca9zxO3lzJXCS1RoC4sWg9Q-9HpyODC9H9BfuIZYHbeAL_fR4q-eKUHj1bnh-lTdYsaoMMQ592oq5EWW9jxp76qhPfvsdGXjonxnQHIOWuBFPLnYB1QgY-qjdeSq3eO3-9aaWXvUGP4oKgqlwFRKQpeWX-HWjf_xhASkfBwheNXPJl9jXw5ahR-u3JibqO2hthZZPh83cgO5g2wsI1UdUp_nqmXPrNFRs6Ve_ZXCtqKSTbfAWTLqe7_-_1nDOw"""


}
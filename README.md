# urus-sewa
UrusSewa systems: A Java-Based Internal Fleet Management System utilizing OOP Principles. To maintain consistency of bookkeeping the car rental inventory, a Graphic User Interface (GUI) program for managing car rental inventory and service has been proposed.

## Run

```bash
javac src/UrusSewaApp.java
java -cp src UrusSewaApp
```

Default staff login:
- Username: value of `URUSSEWA_STAFF_USER` (defaults to `staff`)
- Password hash: value of `URUSSEWA_STAFF_HASH` (SHA-256 hex)

Example setup:
```bash
export URUSSEWA_STAFF_USER=staff
export URUSSEWA_STAFF_HASH=$(python -c "import hashlib; print(hashlib.sha256('YourStrongPassword'.encode()).hexdigest())")
java -cp src UrusSewaApp
```

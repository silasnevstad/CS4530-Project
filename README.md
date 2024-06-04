# CS4530 - HuskSheets<br><sup>Group 12</sup>
HuskSheets project code for CS4520

## Setup Instructions

### Prerequisites

Ensure you have the following installed on your system:
- Java JDK
- Maven
- OpenSSL

### Cloning the Repository
Clone the repository using the following command:

```bash
git clone <repository-url>
cd <repository-directory>
```

### Decrypting the Keys
After cloning the repository, you need to decrypt the keystore, certificate, and truststore files to use them for the server.

Run the decrypt.sh script to decrypt the keys:

```bash
chmod +x decrypt.sh
./decrypt.sh
```
Enter the decryption password when prompted (will be provided in Teams).

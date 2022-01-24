# Document Verification SDK Integration and Usage

Detailed overview on how document verification can be included and used in your own apps.

## Usage overview inside mobile apps

1. Integration
2. Create a singleton instance
3. Get SDK token from server
4. Initialize SDK with token that we got in above step
5. Get supported countries list
6. Ask user to select a country or pre-fill country
7. Selected document on the basis of selected country
8. Start document scan process
9. Handle different events in document scan process
10. Add loader screen when SDK switches to Face scan
11. Handle events in face scan process
12. Get workflow ID after document and face scan is complete
13. Send workflow ID to server
14. Handle credential offer for the document
15. OR handle error message for the particular scan

## Usage overview inside server side

1. Integration
2. Create API endpoint for generating SDK token
3. Create API endpoint to create invitation
4. Create API endpoint to get workflow ID and issue credential OR send error message

## [Android](./android.md)

## [iOS](./ios.md)

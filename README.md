# Drug side to side interaction app

## Description
A drug interaction analysis application that works with a MySQL database. It provides the possibility to connect either locally or via an MQTT server to a remote database.

The program was originally designed to work with the [TwoSIDES](https://nsides.io/) database from Tatonetti Lab. However, with minor code modifications, it can be extended to work with other MySQL databases as well.

The application allows users to:
- Search for available drugs
- View drug interactions, their severity, and related statistics
- Display a Wikipedia description of symptoms
- Use dynamic navigation (e.g., click on headers to jump to relevant sections)

## Program Execution Instructions
The application can be run using one of the following methods:

- Through the GUIStarter class
- Using the run.bat file
# Detailed Design Document
____

## Common package

###DataObject

###User

###OrgUnit

###Asset

###InventoryKey

###InventoryRecord

###Order

###BuyOrder

###SellOrder

###DataPacket

###Custom exceptions
####DoesNotExist
####AlreadyExists
####ConstraintException
####NotAuthorised
####OrderException
####IllegalString
####InvalidAmount
####InvalidDate
####InvalidPrice

###Enums
####DatabaseTables
####ProtocolKeywords


## Protocol
The protocol connecting the server and client programs works like this:
* The sockets used are governed by the PORT final variables in NetworkServer and NetworkDataSource
* The server is expected to run on the address specified in NetworkDataSource.HOSTNAME

## Server program

###ServerGUI
*Class desc:* Where the server-side program starts running.
Has a basic GUI for debug purposes

**main()**<br/>
*Method desc:* Initialises the server-side GUI

###NetworkServer
This class is the back end of the server side. It deals with information
sent over the connection and also automatically resolves trades every 5 minutes. 

###Database
For this assignment a MariaDB database will be used to store six tables of data:
- [Users](#users-table)
- [OrgUnit](#orgunit-table)
- [Inventories](#inventories-table)
- [AssetInfo](#assetinfo-table)
- [BuyOrder](#buyorder-table)
- [SellOrder](#sellorder-table)

The relationships between these tables can be seen in the below ERD diagram.

![image](Diagrams/DatabaseERD2.png)

The line types encode this information about the relations in the database:
- All relations are 1-M (a PK value may have arbitrarily many FK references)
- All relations are optional (a PK value may have 0 FK references),
  but all FK columns except for BoughtFrom and User.OrgUnit are `NOT NULL`


### Users Table
This table is used to store user data. It has four columns:
- Username (string, PK)
- Password (string)
- OrgUnit (string, FK referencing OrgUnit.OrgUnitName)
- AdminAccess (boolean)
- SaltString (string)

The password is stored as a hash, made using the salt string, so that plaintext
passwords are never sent between the client and server programs.
Ideally, the salt string would live in a separate table, but this
project's scope is too small for this to be worthwhile.

### OrgUnit Table
This table is used to store all the organisational units that use the app, and
their credit balance. It has two columns:
- OrgUnitName (string, PK)
- Credits (int)

### AssetInfo Table
This table is used to store the assets that can be traded in the application.
It has two columns:
- AssetID (int)
- AssetDesc (String)

### Inventories Table
This table stores information about the quantities of assets owned
by OrgUnits. Because "OrgUnit owns asset" is a logical many-many relationship,
this table will have a composite key: an organisational unit name, and an asset ID, may
each appear arbitrarily many times, but only one record may exist for any
given OrgUnit-Asset pair. The table has three columns:
- OrgUnit (string, partial key, FK referencing OrgUnit.OrgUnitName)
- AssetID (int, partial key, FK referencing AssetInfo.AssetID)
- Quantity (int)


### SellOrder Table
This table is used to track all the placed sell orders.
It has seven columns:
- OrderID (int, PK)
- Seller (string, FK referencing Users.Username)
- Asset (int, FK referencing Asset.AssetID)
- AskingPricePerAsset (int)
- CurrentQty (int)
- DatePlaced (date)
- DateResolved (date, will be null if order is currently outstanding)

### BuyOrder Table
This table is used to track all the placed buy orders.
It has eight columns:
- OrderID (int, PK)
- Buyer (string, FK referencing Users.Username)
- Asset (int, FK referencing Asset.AssetID)
- MaxPricePerAsset (int)
- RequestedQty (int)
- DatePlaced (date)
- DateResolved (date, will be null if order is currently outstanding)
- BoughtFrom (int, FK referencing SellOrder.OrderID)

The reason for this asymmetry is that the system will resolve matching
BuyOrders and SellOrders where the SellOrder has a higher quantity,
so it makes the most sense to model it like this.


## Client program
###Main
This is where client execution will start. For debug purposes, if run with an args array containing only "MOCK", the
mock database will be used instead of the real one.
###TradingAppGUI
This is the GUI class. It is a Swing GUI consisting of the following:

###NetworkDataSource

##GuiSearch
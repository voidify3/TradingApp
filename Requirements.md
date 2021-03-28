# Client Requirements 
## Functionality
The application must have the following items implemented for basic functionality:
- Organisational Units
- Credits
- Orders
- Assets
- Individual Users

For extended functionality the application should have:
- GUI
- Current offers for a given asset (Buy/Sell Price, QTY)
- Historical Trades graph

The application and associated Databases are to be stored on a singular Client-Server. 
The server is to contain two (2) Databases, User Information, and Organisational Unit Information,  to store the applications information.
The databases are to be in one of the following database formats:
- MariaDB
- PostgreSQL
- SQLite3

The **User Information** database is to store the following Individual User information:
- Username
- Password
- Account Type (User/Administrator)
- Organisational Unit

The **Organisational Unit Information** database is to store the following Organisation Information:
- Organisation Name
- Number of Credits 
- Assets
- Quantity of Individual Assets
- Current Trades
    - Trade Type (Buy/Sell)
    - 2nd Party Information (Organisation Name)
    - Asset
    - Quantity 
    - Price
    - Date

## Organisational units
Organisational Units are equivalent to departments or companies and must have the following feature:

- Credit Balance
- Assets
- Individual Asset Quantities
- Orders

## Credits
Credits are used as a common currency between Organisational Units.
The quantity of credits should be stored per individual Organisational Unit.

***Assumption:*** Credits should be a whole number, no decimal places, to allow for easy storing and manipulation. Client & user feedback on how credits are used should be used to form this data type.

## Assets
Assets are the items that are traded between Organisations.
There is no limit to the number of Assets that an Organisation can have or the quantity of a given asset.

## Orders
Orders are used to track the trades between organisation units. There is no limit to the number of orders that can be listed by either a user or organisation. 
There are to be two (2) types of orders, Buy orders & Sell orders. 

**Buy Orders** are a request from one organisation to another to *Buy* a *Quantity* of an *Asset* for a certain number of *Credits*

**Sell Orders** are a request from one organisation to another to *Sell* a *Quantity* of an *Asset* for a certain number of *Credits*

When reconciling trades, the lowest prices must be used in the trade. 
As such the sell price of an asset can be *less* than the requested buy price.
If the buy price is *greater* than the sell price, the sell price is used to complete the trade.

An order cannot be processed if the buy price is too high for the buy order, of if the sell price is too low for the order.

All orders *must* be automatically reconciled by the system. 

If an order exceeds either the total credits an organisation has (Buy Order), or the total number of a given Asset (Sell Order) the system must ***not*** process the order and should give some feedback to the user to explain why the order was not processed.

### Orders - Nice to Haves
Users should be able to view a list of current orders that have not yet been processed. Using this list users should be able to cancel an order. 

A graph showing the historical trading data for the given asset. The graph should contain the following items:
- X Axis - Date & Time
- Y Axis - Price

Current offers including:
- Buy/Sell price
- Quantity

## Individual Users
Individual users are the personnel that will be placing & requesting the trades. There is to be no limit to the number of users. Users will neeed to be able to complete the following functions:
- Placing Buy/Sell Orders
- View current organisational orders
- Cancelling orders
- View current offers for a given asset
- View historical trading information for a given asset

Each user will be part of an Organisational Unit. There can be no users without an associated Organisational Unit.
Users will inherit their organisations credits. As such, the cost of trades will be subtracted from the organisations total, and  the profit from sales will be added to the organisations total.

Each user will have their own unique username and password. Users should be allowed to change thier own passwords without external assistance. 

### Administrators
There should also be a second user type for administrators.
Administrators are to have the ability to:
- Edit the number of credits that an organisation has (Confirm if this is any or if it it their specific organisation)
- Edit the Quantity of a given asset that an organisation has
- Add a new asset to an organisation
- Add new users
    - Assign Usernames & Passwords
    - Assign Organisational Units to Users
- Create new Administrator Users with the same level of access


## GUI Requirements
- Users to recieve a message upon fulfilment of an order.
- Current orders graph 

![img.png](Images/GUI References/img.png)

- Historical trades Graph

![img_1.png](Images/GUI References/img_1.png)


## Additional Documentation
### Configuration File
The configuration file for the application is to contain the IP address and Port Number of the server that the application is hosted on.
Additionally the configuration file for the Server is to also contain these details.

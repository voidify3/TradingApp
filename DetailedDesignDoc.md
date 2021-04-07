# Detailed Design Document
____
## OVERALL FUNCTIONALITY

## Public class Main
*Class desc:* Where the program starts running from.

*Method desc:* Where the user will log in. User's will have to input the
correct username and password. Exceptions will be thrown if the login details are
incorrect/invalid. If successful, the method will check if the user is part of the 
IT Admin team or a general user. If the user is part of the IT Admin team, they will 
have access to a series of authorised methods. If they are a general user, the method 
will check if they are part of an organisational unit. If they are, they will have 
access to a series of trading methods. If they are not, an exception will be thrown
asking the user to contact their IT Administrator to join an organisational unit<br />
*@throws* Exception if username not found<br />
*@throws* Exception if password does not match username<br />
*@throws* Exception if user is not part of an organisation/IT Admin team<br />
**Public void main()**

    - While loop (while not logged in)
    - Request username through input
    - Checks login details HashMap collection from the database 
      (reminder: username is the key)
    - IF username does not exist throw an exception
    - IF username exists in the database temporarily store the username  
      and request the password through input
    - Request password input
    - IF password does not match username throw an exception,
      clear temporarily stored username and loop back
    - IF password matches username check the access level
    - IF the access level is User, check if the user is part of an organisational unit
    - IF the user is not part of any organisational unit throw an exception
      ("contact your IT Administrator to join an organisational unit)
    - IF the access level is Admin, list the Admin interface methods

## Public interface User
*Class desc:* Abstract (since an interface) that comprises a 
series of methods that a user would call.

*Method desc:* To view quantity of an organisational unit's asset. The user 
calls method with the asset name, the method will check that organisation's 
assets for the queried asset. If the asset is found, method will finally print 
the quantity.<br />
*@param* String for the name of the queried asset<br />
*@throws* Exception if the asset is not found (does not exist)<br />
**Public int viewAssetQuantity(String queriedAsset)**

    - Quiries the user's organisational unit's collection of assets 
      (reminder: a collection with no duplicates)
      (collection will be asset objects with a name & type)
    - IF the asset is found, print the quantity as a string
    - IF the asset is NOT found, throw an exception

*Method desc:* To view available credits of an organisational unit. The user
calls this method with no parameters. The method will print the available credits.<br />
*@returns* Integer for the amount of available credits<br />
**Public int viewAvailableCredits()**

    - Quiries the user's organisational unit's current available credits
    - Prints the available credits for that unit as a string

*Method desc:* To view current outstanding orders before they are executed, the
user calls this method with no parameters. The method will check the database for
the outstanding orders (temporary data). All temporary orders linked to the organisation
will be retrieved and stored in an array list (possible to sort via date but not required). 
From here, the method will loop through the data printing each outstanding order (date/time, 
quantity, price, type of BUY/SELL, trade ID).<br />
*@throws* Exception if no outstanding orders are found<br />
**Public void viewOrders()**

    - Quiries the temporary outstanding trade data for outstanding
      trades linked to the organisational unit.
    - If the organisational unit has no outstanding trades, simply
      throw an exception.
    - If at least 1 outstanding trade is found, store it in an 
      array list (possibly sorting) and using a loop, print each 
      trade out (date/time, quantity, price, type of BUY/SELL, trade ID).

*Method desc:* Method user calls to place a buy order. Method extends a general
ORDER method, using polymorphism to automatically store the trade info in a
specific database (if this method is called, trade info will be stored in a
BUY data section which is ultimately part of the outstanding trade data).
This will separate BUY/SELL trades so matching and executing trades can be
done with ease. The user must specify the asset and quantity they are requesting
to BUY along with the price they're willing to pay for.<br />
*@throws* Exception if the asset does not exist in the system<br />
*@throws* Exception if the organisational unit does not enough available credits<br />
*@throws* Exception if the offer price is out of bounds <br />
**Public void buyOrder(asset, quantity, price)**

    - Check whether the asset type exists (does not mean if the asset is currently 
      being sold, instead making sure the asset type has been added to 
      the system by an IT Administrator).
    - IF the asset does not exist, throw an exception.
    - IF the asset exists, temporarily store the asset type for the order.
    - Temporarily store the quantity for the order.
    - Check wheteher the price is within the bounds.
    - IF the asking price is out of bounds throw an exception.
    - IF the asking price is within bounds, temporarily store the price.
    - Check whether the organisational unit's available funds >= price they 
      are willing to pay.
    - IF the price is larger than the amount of available credits, throw an exception.
    - IF the price is less than or equal to the available credits, temporarily 
      store the price for the order.
    - IF the above checks out, send this information into the database, the database
      should generate a unique trade ID (to keep other methods in the system functional)
    - Decrease the organisational unit's available credits with the credits they offered
      in the BUY order (this will be added back if the trade is canceled before execution).
    - Print a message to alert the user the trade offer has been submitted.

*Method desc:* Method user calls to place an order. Method extends a general
ORDER method, using polymorphism to automatically store the trade info in a
specific database (if this method is called, trade info will be stored in a
SELL data section which is ultimately part of the outstanding trade data).
This will separate BUY/SELL trades so matching and executing trades can be
done with ease. The user must specify the asset and quantity they are requesting
to BUY along with the price they're willing to pay for.<br />
*@throws* Exception if the asset does not exist in the system<br />
*@throws* Exception if the organisational unit does not have the asset<br />
*@throws* Exception if the organisational unit does not have enough (quantity) of the asset<br />
*@throws* Exception if the asking price is out of bounds<br />
**Public void sellOrder(asset, quantity, price)**

    - Check whether the asset type exists (does not mean if the asset is currently 
      being sold selling, instead making sure the asset type has been added to 
      the system by an IT Administrator).
    - IF the asset does not exist, throw an exception.
    - IF the asset exists, check if the organisational unit has this asset
      (quantity at least >1).
    - IF the organisational unit has the asset, check if the quantity is <= quantity
      user wants to sell.
    - IF the sell quantity is greater than the available quantity throw an exception.
    - IF the available quantity is less than or equal to the quantity the user wants 
      to sell, temporarily store the asset type for the order.
    - Temporarily store the quantity for the order.
    - Check if the asking price is in bounds (greater than zero)
    - IF the asking price is out of bounds throw an exception.
    - IF the asking price is within bounds, temporarily store the price.
    - IF the above checks out, send this information into the database, the database
      should generate a unique trade ID (to keep other methods in the system functional)
    - Decrease the organisational unit's quantity of the asset they listed to sell in 
      the SELL order (this will be added back if the trade is canceled before execution).
    - Print a message to alert the user the trade offer has been submitted.

*Method desc:* Method user calls if they want to cancel an outstanding order.
User should first use the viewOrders method to see all outstanding orders along
with their unique trade ID. From here, the user passes the unique trade into
the method, where the method will then delete all temporary data for that trade
and return the quantity or credits back (depending if the outstanding trade was
listed as BUY or SELL).<br />
*@param* Integer for the trade ID to cancel the correct and specific trade<br />
*@throws* Exception if the trade ID does not exist<br />
**Public void cancelOrder(tradeID)**

    - Match the quiried trade ID with the one in the outstanding order database.
    - IF the trade ID is not found throw an exception.
    - IF there is a match, ask the user if they are sure they want to cancel the trade.
    - IF the user responds with "No", break from the method.
    - IF the user responds with "Yes", delete the outstanding trade info and return the 
      asset quantity or credits (depending if BUY or SELL trade).

*Method desc:* Method user calls to view the average BUY/SELL price of an asset 
type throughout the entire company, not including outstanding trade data. A 
time frame can be selected by the user to see the average price over any 
specified period. If the time frame is invalid, an exception will be thrown. <br />
*@throws* Exception if the asset does not exist in the system<br />
*@throws* Exception if dateFrom is invalid <br />
*@throws* Exception if dateTO is invalid <br />
**Public void viewAveragePrice(assetType, dateFrom, dateTo)**

        - Check whether the asset type exists (does not mean if the asset is currently
          being sold selling, instead making sure the asset type has been added to
          the system by an IT Administrator).
        - IF the asset does not exist, throw an exception.
        - IF the asset does exist, check if the given time frame is invalid.
        - IF dateFrom is after dateTo, throw an exception.
        - IF dateTo is after the current date, throw an exception.
        - IF dateFrom is earlier than the first stored order data, fill with empty data.
        - IF time frame is valid, gather all price data within time frame in an array
          using a loop.
        - Calculate the average price.
        - Print the price to the user as a double.

*Method desc:* Method user calls to view the visual price history of a 
specified asset type throughout the entire company, not including outstanding 
trade data. A time frame can be selected by the user to see the average price 
over any specified period. If the time frame is invalid, an exception will 
be thrown. <br />
*@throws* Exception if the asset does not exist in the system<br />
*@throws* Exception if dateFrom is invalid <br />
*@throws* Exception if dateTO is invalid <br />
**Public void viewPriceHistory(assetType, dateFrom, dateTo)**

        - Check whether the asset type exists (does not mean if the asset is currently
          being sold selling, instead making sure the asset type has been added to
          the system by an IT Administrator).
        - IF the asset does not exist, throw an exception.
        - IF the asset does exist, check if the given time frame is invalid.
        - IF dateFrom is after dateTo, throw an exception.
        - IF dateTo is after the current date, throw an exception.
        - IF dateFrom is earlier than the first stored order data, fill with empty data.
        - IF time frame is valid, gather all order data within the time frame in array 
          using a loop, including price, time of execution and quantity.
        - Display a graphical plot of price over time with quantities.

*Method desc:* Method user calls to change their password. User must enter their 
current password before being prompted to enter a new password. The password 
must be successfully confirmed, and must also meet the requirements of 
password length and character usage. The login details HashMap collection 
is then updated. <br />
*@throws* Exception if the current password is incorrect <br />
*@throws* Exception if the new password is invalid <br />
*@throws* Exception if the confirmed password does not match the new password <br />
**Public void changePassword()**

        - Prompt the user to enter their current password.
        - Check if the entered string matches the users current password.
        - IF the string does not match, throw an exception.
        - IF the string does match, prompt the user to enter their new password.
        - Check if the entered string meets the password requirements 
          (i.e. minimum character count).
        - IF the string does not meet the requirements, throw an exception.
        - IF the string is valid, prompt the user to confirm the new password.
        - Check if the entered string matches the previous string.
        - IF the strings are not equal, throw an exception.
        - IF the strings are equal, update the login details HashMap collection
          for the user.

## TODO: Public interface Admin
*Class desc:* Abstract (since an interface) that comprises a
series of methods that an IT Admin would call.

*Method desc:* Method used to create a new unique username with a password 
and access level. The method will check that the username does not exist 
in the database already, before assigning it the given password and access 
level. By default, the user will not be part of any organisational unit. <br />
*@throws* Exception if the username already exists <br />
*@throws* Exception if the password is invalid <br />
*@throws* Exception if the access level is invalid (must be "User" or "Admin") <br />
**void createUser(userName, password, accessLevel)**

        - Check to see if username already exists in the database.
        - IF username exists, throw exception.
        - IF username does not exist, check if password is valid.
        - IF password is not vaild, throw exception.
        - IF password is valid, check if access level is valid.
        - IF access level is invalid, throw exception.
        - IF access level is valid, add username details to login details HashMap 
          collection.

*Method desc:* Method used to generate a new password for users who have 
forgotten theirs. The username will be checked to see if it exists in the 
database, and the password will be checked to see if it is valid. <br />
*@throws* Exception if the username does not exist <br />
*@throws* Exception if the password is invalid <br />
**void generatePassword(userName, password)**

        - Check to see if username exists in the database.
        - IF username does not exist, throw exception.
        - IF username exists, check if password is valid.
        - IF password is not vaild, throw exception.
        - IF password is valid, update username details in the login details 
          HashMap collection.

*Method desc:* Method used by IT admins to set the access level of an existing 
user. This can be used if a general users joins the IT administration team, or 
vice versa. <br />
*@throws* Exception if the username does not exist <br />
*@throws* Exception if the access level is invalid (must be "User" or "Admin") <br />
**void setAccess(userName, accessLevel)**

        - Check to see if username exists in the database.
        - IF username does not exist, throw exception.
        - IF username exists, check if access level is valid.
        - IF access level is invalid, throw exception.
        - IF access level is valid, update username details in the login details 
          HashMap collection.

*Method desc:* Method used by IT admins to assign a user to an organisational 
unit. By default, users are not assigned to a unit, so this method will be 
called at least once for each user. The method can also be called when a user 
changes to a new organisational unit, where confirmation will be required to
avoid accidental changes. <br />
*@throws* Exception if the username does not exist <br />
*@throws* Exception if the organisational unit does not exist <br />
**void assignToUnit(userName, orgUnit)**

        - Check to see if username exists in the database.
        - IF username does not exist, throw exception.
        - IF username exists, check if organisational unit exists.
        - IF organisational unit does not exist, throw exception.
        - IF organisational unit does exist, check if username is assigned
          to an organisational unit already.
        - IF the username is not assigned to a unit, update username details.
        - IF the username is already assigned to a unit, prompt user for confirmation
          by entering "Yes" or "No"
        - IF the user enters, "No", break from method.
        - IF the user enters, "Yes", update username details.

*Method desc:* Method used by IT admins to change the amount of a specified
asset within an organisational unit. This can be used to make corrections to 
asset amounts when an error has occurred. All BUY/SELL orders within the unit 
made with this asset type will be during this method. IT admins may want to 
give appropriate notice of this change to prevent any issues.<br />
**(NOTE: is there a better way to modify the assets without cancelling orders?)** <br />
*@throws* Exception if the organisational unit does not exist <br />
*@throws* Exception if the asset does not exist in the system <br />
*@throws* Exception the amount is out of bounds <br />
**void modifyAsset(orgUnit, asset, amount)**

        - Check if organisational unit exists.
        - IF organisational unit does not exist, throw exception.
        - IF organisational unit does exist, check if asset exists.
        - IF asset does not exist, throw exception.
        - IF asset does exist, check if amount is out of bounds.
        - IF amount < 0, throw exception.
        - IF amount >= 0, use a loop with cancelOrder() to cancel all orders
          containing the specified asset.
        - Overide asset amount for organisational unit.

*Method desc:* Method used by IT admins to change the amount of credits an 
organisational unit has. This can be used to make corrections to credit amounts 
when an error has occurred, or to add/subtract credits for other reasons.
All BUY/SELL orders within the unit will be during this method. IT admins 
may want to give appropriate notice of this change to prevent any issues. <br />
**(NOTE: is there a better way to modify credits without cancelling orders?)** <br />
*@throws* Exception if the organisational unit does not exist <br />
*@throws* Exception the amount is out of bounds <br />
**void modifyCredits(orgUnit, amount)**

        - Check if organisational unit exists.
        - IF organisational unit does not exist, throw exception.
        - IF organisational unit does exist, check if amount is out of bounds.
        - IF amount < 0, throw exception.
        - IF amount >= 0, use a loop with cancelOrder() to cancel all orders
          within the unit.
        - Overide credit amount for organisational unit.

*Method desc:* Method used by IT admins to create a new organisational unit. 
The method will check if the organisational unit exists in the systems
already, before adding it to the database. By default the organisational unit 
will have zero credits and zero assets. <br />
*@throws* Exception if organisational unit already exists. <br />
**void createUnit(orgName)**

        - Using a loop, check if organisational unit exists in the system 
          already.
        - IF the unit exists, throw exception.
        - IF the unit does not exist, add the new unit to the database
          with 0 credits and 0 of all assets.

*Method desc:* Method used by IT admins to create a new asset type. The 
method will check if the asset type exists in the systems already, before 
adding it to the database. <br />
*@throws* Exception if asset type already exists. <br />
**void newAssetType(assetName)**

        - Using a loop, check if asset type exists in the system already.
        - IF the asset type exists, throw exception.
        - IF the asset type does not exist, add the new asset to the database.



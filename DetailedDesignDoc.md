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

##Public interface User
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

*Method desc:* Method user calls to place an order. Method extends a general
ORDER method, using polymorphism to automatically store the trade info in a
specific database (if this method is called, trade info will be stored in a 
BUY data section which is ultimately part of the outstanding trade data). 
This will separate BUY/SELL trades so matching and executing trades can be 
done with ease. The user must specify the asset and quantity they are requesting 
to BUY along with the price they're willing to pay for.<br />
*@throws* Exception if the asset does not exist in the system<br />
*@throws* Exception if the organisational unit does not enough available credits<br />
**Public void buyOrder(asset, quantity, price)**

    - Check whether the asset type exists (does not mean if the asset is currently 
      being sold selling, instead making sure the asset type has been added to 
      the system by an IT Administrator).
    - IF the asset does not exist, throw an exception.
    - IF the asset exists, temporarily store the asset type for the order.
    - Temporarily store the quantity for the order.
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
    - IF there is a match, delete the outstanding trade info and return the 
      asset quantity or credits (depending if BUY or SELL trade)

## TODO: Public interface Admin
*Class desc:* Abstract (since an interface) that comprises a
series of methods that an IT Admin would call.



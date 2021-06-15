#TODO
* Fix database
    - [x] Fix code
    - [x] Fix ERD
    - [x] Fix class diagram
    - [ ] Merge
* Fix the class diagram (ensure arrows are correct)
    - [x] add package shapes
    - [x] add enums
    - [x] add data interface shape
    - [x] add method lists to data source
    - [ ] add method lists to tradingappdata
    - [ ] update to reflect GUI structure
* FIRST PRIORITY: implement GUI pages as specified, adding tradingappdata content and error handling on the way
    - [x] home page/admin portal system
    - [x] admin home has unit and asset bars
    - [x] change own password page (available from menu bar)
    - [x] unit editing page (can change credits)
    - [x] asset editing page (can change description)
    - [x] user editing page with password box and unit dropdown and access radio
    - [x] place order page (asset dropdown, quantity spinner, price spinner)
    - [x] place sell order page
    - [x] convert home page
    - [x] branch off for GUI changes
    - [x] order list page
        - [x] order list toggles
        - [x] order list clickthrough
    - [x] asset list page class
        - [x] clickthrough to info page
    - [x] inventory list page class
        - [x] search boxes and radio buttons
        - [x] clickthrough to an input dialog
        - [x] create new record button
* SECOND PRIORITY:
    - [ ] user list page class
    - [ ] convert change password page
    - [ ] convert login page
    - [ ] convert shell panel
    - [ ] `TradingAppGUI extends JFrame`
    - [ ] JavaDoc for TradingAppData
    - [ ] unit tests for TradingAppData
    - [ ] unit test file for object class AssertThrows cases
    - [ ] Fix order placing problems
* [ ] THIRD PRIORITY: have socket and hostname read from a file
* FOURTH PRIORITY: implement GUI content and protocol contingency for resolution notification
    - [x] third type of SPECIAL query, executed at data source setup, returning how long until the next res time
    - [ ] Swing timer: every 5 minutes, with an initial delay of slightly more than this number to account for slowness, get
      `buyOrdersResolvedBetween(now.minusMinutes(5), now)` and `sellOrdersReconciledBetween(now.minusMinutes(5), now)`
      and also provide the option to refresh db info on current screen
    - [ ] Logic to generate user-friendly summary
    - [ ] display mini summary in a row of the screen, clickable to view full summary
* [ ] FIFTH PRIORITY: implement graph view

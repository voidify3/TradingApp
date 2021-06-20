#TODO
* Fix database
    - [x] Fix code
    - [x] Fix ERD
    - [x] Fix class diagram
    - [x] Merge
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
    - [x] EDIT TRADE RESOLUTION TO USE AN UPDATABLE RESULT SET
    - [x] convert change password page
    - [x] convert login page inner part
    - [x] convert menu bar
    - [x] convert shell panel first row and get rid of guilisteners
    - [x] `TradingAppGUI extends JFrame`
    - [x] JavaDoc for TradingAppData
    - [x] Fix order placing bug!!!
* FRIDAY
  - [x] create unit test file for data object validation
  - [x] create build script file
  - [x] dot points of video scripts
  - [x] check that trade rec actually happens
  - [x] generate javadoc for everything
* SATURDAY  
  - [x] finish unit tests for TradingAppData
  - [x] write unit tests for data object validation
  - [x] record individual audio
  - [x] rehearse group video for time
  - [x] write build script
  - [x] finish javadoc
SUNDAY
  - [ ] record scrolling footage
  - [x] add outstanding order info to place order page
  - [ ] record easy part of main audio
  - [ ] record easy part of main footage
  - [ ] record demonstration footage
  - [ ] record demonstration audio in the editing stage
  - [ ] edit videos
  
##TASKS BELOW THIS POINT ARE HEREBY CANCELLED FOR TIME REASONS
* [ ] THIRD PRIORITY: have socket and hostname read from a file
* FOURTH PRIORITY: implement GUI content and protocol contingency for resolution notification
    - [x] third type of SPECIAL query, executed at data source setup, returning how long until the next res time
    - [ ] Swing timer: every 5 minutes, with an initial delay of slightly more than this number to account for slowness, get
      `buyOrdersResolvedBetween(now.minusMinutes(5), now)` and `sellOrdersReconciledBetween(now.minusMinutes(5), now)`
      and also provide the option to refresh db info on current screen
    - [ ] Logic to generate user-friendly summary
    - [ ] display mini summary in a row of the screen, clickable to view full summary
* [ ] FIFTH PRIORITY: implement graph view
* SIXTH PRIORITY: make everything nice and cute
    - [ ] convert login page outer part
    - [ ] convert shell panel
    - [ ] make factory use consistent
    - [ ] observer pattern for manual resolution

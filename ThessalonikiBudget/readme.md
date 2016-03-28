#Read me
######What this application does
This application reads from budget page of the Thessaloniki City Council, the revenues and expenses and saves a csv file with the date and time.

######How to execute the application

To execute the application you have to define the path where the download files will be saved. The command to execute the application is
java -jar ThessBudget.jar [path]

The application will create a folder ThessCity, in the declared folder will create a folder for the revenues files and one for the expenses files. In each of them will create a folder for the current year and under that folder, one for the current month, under which the dail files will be stored. With this format, it is easy to distinguish revenues and expenses files for each month of each year.

If the path to the folder is not writable, the file will be used at the point from where the call was made to the application.

#Application Design 

######prerequisites
The program uses Maven technology to download the necessary libraries such as the UI4J to create a link to the page and can give commands that would give a user.

The libraries are in ThessBudget_lib folder in jar format.

######Description of the problem
We needed a program that in an automated way it could collect data on the progress of the budget of the Municipality of Thessaloniki, as they appear in their web-page

######Description of the solution
The layout of the page that holds the City's budget, does not permit direct reading of the data. For this reason, the UI4J library was used, which recognize all the elements of a web page (buttons, drop down options) and allows their use. It also enables the Headless Mode, thanks to which it can operate on computers without GUI or a web browser. Ideal for the conditions that we want to run the program.

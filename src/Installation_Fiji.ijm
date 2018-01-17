macro "Installation_2018-01-17"{

version = "1.0a 2018-01-17";


//IJ version verification and close the macro's window 
//selectWindow("Installation.ijm");			
//run("Close");						
requires("1.49g");					

//Initialisation of the error counter
Errors=0;

//GUI Message
Dialog.create("Installation wizard for the Lipid_Droplets macro");
Dialog.addMessage("Version\n" + version);
Dialog.addMessage("Cluet David\nResearch Ingeneer,PHD\nCNRS, ENS-Lyon, LBMC");
Dialog.addMessage("This program will install the Lipid_Droplets macro.\nShortcut will be added in the Plugins/Macros menu.");
Dialog.show();

//Prepare key paths
PathSUM = getDirectory("macros")+File.separator+"StartupMacros.fiji.ijm";	
PathFolderInput =File.directory+File.separator+"Macro"+File.separator;					
PathOutput = getDirectory("macros")+"NUKE-BREAK"+File.separator;

//Listing of the files to instal
Listing = newArray();
Listing = Array.concat("Lipid_Droplets.java", Listing);
Listing = Array.concat("Command_Line.txt", Listing);
Listing = Array.concat("Stack_Editing.java", Listing);

//Create the installation folder if required
if(File.exists(PathOutput)==0){
File.makeDirectory(getDirectory("macros")+File.separator+"NUKE-BREAK");
}

//Installation of all files of the listing
for(i=0; i<lengthOf(Listing); i++){
	if(File.exists(PathFolderInput+Listing[i])==0){
		waitForUser("" + PathFolderInput+Listing[i] + " file is missing");
		Errors = Errors + 1;
	}else{
		Transfer=File.copy(PathFolderInput+Listing[i], PathOutput+Listing[i]);
	}
}


//Create the shortcut in IJ macro menu for the first installation (Main program)
PCommandLine = PathFolderInput+ "Command_Line.txt"; 
SUM = File.openAsString(PathSUM);
pos =lastIndexOf(SUM, "//End_Lipid_droplets");
if(pos == -1){
	SUM = SUM + "\n\n" + File.openAsString(PCommandLine); 
	Startup = File.open(PathSUM);
	print(Startup, SUM);
	File.close(Startup);
}


//The program prompts the user of the success or failure of the installation.
if(Errors == 0){
waitForUser("Installation has been performed sucessfully!\nRestart your ImageJ program.");
} else {
waitForUser("Files were missing!\nInstallation is incomplete.");
}

}

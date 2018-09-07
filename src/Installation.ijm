macro "Installation_2018-02-27"{


tag = "v4.0.3"
lastStableCommit = "09c54a63"
myProgram = "Lipid Droplets and REPO Analysis";


//IJ version verification and close the macro's window
selectWindow("Installation.ijm");
run("Close");
requires("1.49g");

//Initialisation of the error counter
Errors=0;

//GUI Message
Welcome(myProgram, tag, lastStableCommit);

//Prepare key paths
PathSUM = getDirectory("macros")+File.separator+"StartupMacros.txt";
PathFolderInput =File.directory+File.separator+"Macro"+File.separator;
PathOutput = getDirectory("macros")+"Droplets"+File.separator;

//Listing of the files to instal
Listing = newArray("Lipid_Droplets.java",
                    "Command_Line.txt",
                    "Stack_Editing.java",
                    "Close_Images.java",
                    "Distribution.java",
                    "Main.java",
                    "Main_GUI.java",
                    "Stats.java",
                    "settings.csv",
                    "Final_report.md",
                    "LayOut.md");

//Create the installation folder if required
if(File.exists(PathOutput)==0){
File.makeDirectory(getDirectory("macros")+File.separator+"Droplets");
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
repair = lastIndexOf(SUM, "Lipid_Droplets.java");
if(pos == -1){
	SUM = SUM + "\n\n" + File.openAsString(PCommandLine);
	Startup = File.open(PathSUM);
	print(Startup, SUM);
	File.close(Startup);
}
if(repair != -1){
	SUM = replace(SUM, "Lipid_Droplets.java", "Main.java");
	Startup = File.open(PathSUM);
	print(Startup, SUM);
	File.close(Startup);
}


//The program prompts the user of the success or failure of the installation.
if(Errors == 0){
DisplayInfo("", myProgram,
		"Installation has been performed sucessfully!<br>Restart your ImageJ program.");
} else {
DisplayInfo("", myProgram,
		"Files were missing!<br>Installation is incomplete.");
}

/*
================================================================================
*/

function DisplayInfo(Titre, NomProg, Message){
    showMessage(Titre, "<html>"
			+"<font size=+3>"
			+"<h1><font color=rgb(77,172,174)>" + NomProg + " INSTALLATION</h1>"
			+"<font size=+0>"
			+"<font color=rgb(0,0,0)>"
            		+"<p>" + Message + "</p>"
			);
}//END DisplayInfo

/*
================================================================================
*/

function Welcome(NomProg, myTag, myCommit){
    showMessage("WELCOME", "<html>"
			+"<font size=+3>"
			+"<h1><font color=rgb(77,172,174)>" + NomProg + " INSTALLATION</h1>"
			+"<font size=+0>"
			+"<font color=rgb(0,0,0)>"
			+"<ul>"
			+"<li>Version: " + myTag + "</li>"
			+"<li>Last stable commit: " + myCommit + "</li>"
			+"</ul>"
			+"<p><font color=rgb(100,100,100)>Cluet David<br>"
            		+"Research Ingeneer,PHD<br>"
            		+"<font color=rgb(77,172,174)>CNRS, ENS-Lyon, LBMC</p>"
			+"<p><font color=rgb(0,0,0)>This program will install the Lipid_Droplets macro.<br>
            		+"Shortcut will be added in the Plugins/Macros menu.</p>"
			);
}//END WELCOME

}

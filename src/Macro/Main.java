macro "Main"{
    //INFOS
    tag = "v2.0.0"
    lastStableCommit = "66ffa5a2"
    gitlaburl = "http://gitlab.biologie.ens-lyon.fr/dcluet/Lipid_Droplets"

    //Welcome
    Welcome(tag, lastStableCommit, gitlaburl);

    /*
    ============================================================================
                                MAIN VARIABLES
    ============================================================================
    */

    //Extension
    myExt = ".czi";
    //Resolution of reference
    ResWref = 0.156;
    ResHref = 0.156;
    //Region of Analysis
    Selections = newArray("Whole tissue",  "Manual ROI");
    //xy threshold between 2 differents Lipid Droplets
    xythreshold = 5;
    xythresholdMicron = xythreshold * ResWref * ResHref;
    //z threshold between 2 different Lipid Droplets
    zthreshold = 5;
    zthresholdMicron = zthreshold * ResWref * ResHref;
    //Initial Low resolution scan
    SizeMin = 7;
    SizeMinMicron = SizeMin * ResWref * ResHref;
    SizeMax = 15000;
    SizeMaxMicron = SizeMax * ResWref * ResHref;
    //False negative removal
    SizeMaxC = 500;
    SizeMaxCMicron = SizeMaxC * ResWref * ResHref;
    CircMinC = 0.5;
    CircMaxC = 1;

    //Number of iteration
    Iterations = 3;
    //Correction factor between iterations
    enlargement = 5;
    //Number of bins for the distributions graphs
    nBins = 50;

    /*
    ============================================================================
                                MAIN GUI
    ============================================================================
    */

    //Initialisation of the Argument
    ARGcommon = "";

    //GUI
    Dialog.create("SETTINGS:");
    Dialog.addString("Extension of the stacks files: ", myExt, 5);
    Dialog.addMessage("Initial resolution used for calibration:");
    Dialog.addNumber("Pixel Width ", ResWref, 3, 5, "micron");
    Dialog.addNumber("Pixel Height: ", ResHref, 3, 5, "micron");
    Dialog.addChoice("Region to process: ", Selections, "Whole tissue");

    Dialog.addMessage("Thresholds between particles (microns):");
    Dialog.addNumber("XY Distance: ", xythresholdMicron, 3, 5, "microns");
    Dialog.addNumber("Z Distance: ", xythresholdMicron, 3, 5, "slices");

    Dialog.addMessage("Parameters for the initial low-resolution scan:");
    Dialog.addNumber("Minimal surface: ", SizeMinMicron, 3, 7, "microns");
    Dialog.addNumber("Maximal surface: ", SizeMaxMicron, 3, 7, "microns");

    Dialog.addMessage("Parameters for the high-resolution scan:");
    Dialog.addNumber("Maximal surface: ", SizeMaxCMicron, 3, 7, "microns");
    Dialog.addNumber("Minimal circularity: ", CircMinC, 3, 5, "");
    Dialog.addNumber("Maximal circularity: ", CircMaxC, 3, 5, "");

    Dialog.addMessage("This program is based on iterative detection of the the brightest particles.");
    Dialog.addNumber("Number of iterations: ", 3);
    Dialog.addNumber("Correction factor: ", 5, 0, 1, "pixel");
    Dialog.addNumber("Number of bins for the distributions: ", nBins, 0, 3, "");
    Dialog.show();

    myExt = Dialog.getString();
    ResWref = Dialog.getNumber();
    ResHref = Dialog.getNumber();

    myChoice = Dialog.getChoice();
    if (myChoice == "Whole tissue"){
        ARGcommon  += "Brain" + "*";
    }else if (myChoice == "Manual ROI"){
        ARGcommon  += "Manual ROI" + "*";
    }
    ARGcommon  += "" + ResWref + "*";
    ARGcommon  += "" + ResHref + "*";

    xythreshold = Dialog.getNumber() / (ResWref * ResHref);
    ARGcommon  += "" + xythreshold + "*";
    zthreshold = Dialog.getNumber() / (ResWref * ResHref);
    ARGcommon  += "" + zthreshold + "*";

    SizeMin = Dialog.getNumber() / (ResWref * ResHref);
    ARGcommon  += "" + SizeMin + "*";
    SizeMax = Dialog.getNumber() / (ResWref * ResHref);
    ARGcommon  += "" + SizeMax + "*";

    SizeMaxC = Dialog.getNumber() / (ResWref * ResHref);
    ARGcommon  += "" + SizeMaxC + "*";
    ARGcommon  += "" + Dialog.getNumber() + "*"; //Circ Min
    ARGcommon  += "" + Dialog.getNumber() + "*"; //Circ Maximal
    ARGcommon  += "" + Dialog.getNumber() + "*"; //Iterations
    ARGcommon  += "" + Dialog.getNumber() + "*"; //Enlarge
    ARGcommon  += "" + Dialog.getNumber() + "*"; //Bins

    /*
    ============================================================================
                            IDENTIFICATION OF THE FILES
    ============================================================================
    */

    //Retrieve folder to explore
    myTitle = "PLEASE CHOOSE THE FOLDER CONTAINING THE FILES TO PROCESS";
    PathFolderInput = getDirectory(myTitle);

    //Generate Finger Print
    getDateAndTime(year,
                    month,
                    dayOfWeek,
                    dayOfMonth,
                    hour,
                    minute,
                    second,
                    msec);
    FP = "" + year + "-" + month + "-" + dayOfMonth + "_";
    FP += "" + hour + "-" + minute + "_";

    //Create text Files
    myAnalysis = PathFolderInput + FP + "_Files.txt";
    Listing = File.open(myAnalysis);
    File.close(Listing);

    myCommands = PathFolderInput + FP + "_Parameters.txt";
    Listing = File.open(myCommands);
    File.close(Listing);

    //Find all files and store path in myAnalysis
    listFiles(PathFolderInput, myExt, myAnalysis);

    //Retrieve number of files
    RawList = File.openAsString(myAnalysis);
    FileList = split(RawList, "\n");

    //Inform user
    showMessage("" + FileList.length + " files have been found.\n"
                + "Press OK when ready for manual pre-processing.");

    /*
    ============================================================================
                            LOOP OF PARAMETERS CREATION
    ============================================================================
    */

    for (myFile=0; myFile<FileList.length; myFile++){
        //Header CREATION
        myHeader = "File " + (myFile+1) + " out of " + FileList.length + ".";

        //Reinitiate ARG
        ARG = ARGcommon;

        //Select image file
        Path = FileList[myFile];

        //Command for Bioformat Importer
        CMD1 = "open=[";
        CMD1 += Path + "]";
        CMD1 += " autoscale";
        CMD1 += " color_mode=Default";
        CMD1 += " rois_import=[ROI manager]";
        CMD1 += " view=Hyperstack stack_order=XYCZT";
        run("Bio-Formats Importer", CMD1);

        Titre = getTitle;

        //Create the Crop movie
        run("Enhance Contrast", "saturated=0.35");
        waitForUser(myHeader +"\nSet on the starting slice");
        Sstart = getSliceNumber();
        waitForUser(myHeader +"\nSet on the ending slice");
        Send = getSliceNumber();

        //Crop the Stack
        PathM1 = getDirectory("macros");
        PathM1 += "Droplets"+File.separator;
        PathM1 += "Stack_Editing.java";

        ARG1 = Titre + "\t";
        ARG1 += "" + Sstart + "\t";
        ARG1 += "" + Send + "\t";

        ARG += "" + Sstart + "*";
        ARG += "" + Send + "*";
        runMacro(PathM1, ARG1);

        setSlice(nSlices);
        waitForUser(myHeader +"\nDraw the neuropil");
        getSelectionCoordinates(NeuroPilX, NeuroPilY);
        NPX = "";
        NPY = "";
        for(i=0; i<NeuroPilX.length; i++){
            NPX += "" + NeuroPilX[i] + "-";
            NPY += "" + NeuroPilY[i] + "-";
        }
        ARG += NPX + "*";
        ARG += NPY + "*";

        ARG += Path;

        //Args = split(ARG, "*");
        //Array.show(Args);
        //waitForUser("");

        //Update the command file
        File.append(ARG, myCommands);
    }

    //Inform user
    showMessage("Press OK when ready for automated analysis.");

    /*
    ============================================================================
                            LOOP OF ANALYSIS
    ============================================================================
    */

    //Retrieve Commands
    Commands = File.openAsString(myCommands);
    CommandsList = split(Commands, "\n");

    for (c=0; c<CommandsList.length; c++){
        //Use the correct concatenated arguments
        ARG = CommandsList[c];

        //Run Lipid_Droplets
        Path = getDirectory("macros");
        Path += "Droplets"+File.separator;
        Path += "Lipid_Droplets.java";
        setBatchMode(true);
        runMacro(Path, ARG);

    }


    waitForUser("Analysis is over");

/*
===============================================================================
                            FUNCTIONS
===============================================================================
*/

function listFiles(folder, extension, outFilePath) {

	list = getFileList(folder);
	for (i=0; i<list.length; i++) {
        if (File.isDirectory(folder+list[i])){
           	listFiles(""+folder+list[i], extension, outFilePath);
       	}

		if (endsWith(list[i], extension)){
            //Only file with a RFP twin are added
            File.append(""+folder+list[i], outFilePath);
		}
	}
}//END LISTFILES

/*
================================================================================
*/

function Welcome(myTag, myCommit, url){
    showMessage("WELCOME", "<html>"
			+"<font size=+3>"
			+"<h1><font color=rgb(77,172,174)>Lipid Droplets Analysis</h1>"
			+"<font size=+0>"
			+"<font color=rgb(0,0,0)>"
			+"<ul>"
			+"<li>Version: " + myTag + "</li>"
			+"<li>Last stable commit: " + myCommit + "</li>"
			+"</ul>"
			+"<p><font color=rgb(100,100,100)>Cluet David<br>"
            +"Research Ingeneer,PHD<br>"
            +"<font color=rgb(77,172,174)>CNRS, ENS-Lyon, LBMC</p>"
			)
}//END WELCOME


}//END MACRO

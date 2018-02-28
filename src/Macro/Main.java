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

    ARG = ARGcommon;

    /*
    ============================================================================
                            LOOP OF BATCH ANALYSIS
    ============================================================================
    */


    //Choose image file
    Path = File.openDialog("Choose file");

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
    waitForUser("Set on the starting slice");
    Sstart = getSliceNumber();
    waitForUser("Set on the ending slice");
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
    waitForUser("Draw the neuropil");
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

    //Run Lipid_Droplets
    Path = getDirectory("macros");
    Path += "Droplets"+File.separator;
    Path += "Lipid_Droplets.java";
    setBatchMode(true);
    runMacro(Path, ARG);

    waitForUser("Analysis is over");

/*
================================================================================
*/

function Welcome(myTag, myCommit, url){
    Dialog.create("WELCOME");
    Dialog.addMessage("Lipid Droplets Analysis")
    Dialog.addMessage("Version: " + myTag);
    Dialog.addMessage("Last stable commit: " + myCommit);
    Dialog.addMessage("Cluet David\nResearch Ingeneer,PHD\nCNRS, ENS-Lyon, LBMC");
    Dialog.addHelp(url);
    Dialog.show();
}//END WELCOME


}//END MACRO

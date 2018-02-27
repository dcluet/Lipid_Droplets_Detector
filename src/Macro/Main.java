macro "Main"{
    //INFOS
    tag = "v1.2.2"
    lastStableCommit = "2683238a"
    gitlaburl = "http://gitlab.biologie.ens-lyon.fr/dcluet/Lipid_Droplets"

    //Welcome
    Welcome(tag, lastStableCommit, gitlaburl);

    //VARIABLES
    /*
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

    //Initialisation of the Argument
    ARG = "";

    //GUI
    Dialog.create("SETTINGS:");
    Dialog.addString("Extension of the stacks files: ", myExt, 5);
    Dialog.addMessage("Initial resolution used for calibration:");
    Dialog.addNumber("Pixel Width ", ResWref, 3, 5, "micron");
    Dialog.addNumber("Pixel Height: ", ResHref, 3, 5, "micron");
    Dialog.addChoice("Region to process: ", Selections, "Whole tissue");

    Dialog.addMessage("Thresholds between particles (microns):");
    Dialog.addNumber("XY Distance: ", xythresholdMicron, 3, 5, "micron");
    Dialog.addNumber("Z Distance: ", xythresholdMicron, 3, 5, "micron");

    Dialog.addMessage("Parameters for the initial low-resolution scan:");
    Dialog.addNumber("Minimal surface: ", SizeMinMicron, 3, 7, "micron");
    Dialog.addNumber("Maximal surface: ", SizeMaxMicron, 3, 7, "micron");

    Dialog.addMessage("Parameters for the high-resolution scan:");
    Dialog.addNumber("Maximal surface: ", SizeMaxCMicron, 3, 7, "micron");
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
        ARG += "Brain" + "\t";
    }else if (myChoice == "Manual ROI"){
        ARG += "Manual ROI" + "\t";
    }
    ARG += "" + ResWref + "\t";
    ARG += "" + ResHref + "\t";

    xythreshold = Dialog.getNumber() / (ResWref * ResHref);
    ARG += "" + xythreshold + "\t";
    zthreshold = Dialog.getNumber() / (ResWref * ResHref);
    ARG += "" + zthreshold + "\t";

    SizeMin = Dialog.getNumber() / (ResWref * ResHref);
    ARG += "" + SizeMin + "\t";
    SizeMax = Dialog.getNumber() / (ResWref * ResHref);
    ARG += "" + SizeMax + "\t";

    SizeMaxC = Dialog.getNumber() / (ResWref * ResHref);
    ARG += "" + SizeMaxC + "\t";
    ARG += "" + Dialog.getNumber() + "\t"; //Circ Min
    ARG += "" + Dialog.getNumber() + "\t"; //Circ Maximal
    ARG += "" + Dialog.getNumber() + "\t"; //Iterations
    ARG += "" + Dialog.getNumber() + "\t"; //Enlarge
    ARG += "" + Dialog.getNumber() + "\t"; //Bins





    Args = split(ARG, "\t");
    Array.show(Args);
    waitForUser("");



    //Run Lipid_Droplets

    Path = getDirectory("macros");
    Path += "Droplets"+File.separator;
    Path += "Lipid_Droplets.java";
    runMacro(Path, ARG);

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

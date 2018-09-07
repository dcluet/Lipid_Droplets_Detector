macro "Refresh" {

    //Prepare key paths
    Path = File.separator()+"home.users";
    Path += File.separator()+"dcluet";
    Path += File.separator()+"Programmes";
    Path += File.separator()+"2017_Lipid_Droplets_Brain_Droso";
    Path += File.separator()+"src";

    PathFolderInput = Path+File.separator+"Macro"+File.separator;
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

    //Update files or create them
    for(i=0; i<lengthOf(Listing); i++){
    	if(File.exists(PathFolderInput+Listing[i])==0){
        	exit("" + PathFolderInput+Listing[i] + " file is missing");
    	}else{
			Transfer=File.copy(PathFolderInput+Listing[i], PathOutput+Listing[i]);
		}
	}

    //Launch main macro
    Path = PathOutput + "Main.java";
    runMacro(Path);

}

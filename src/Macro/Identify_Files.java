macro "Identify_Files"{

    /*
    Identify the files with the correct extension and store the paths
    in myAnalysis.
    */

    Argument = getArgument();
    Arguments = split(Argument, "*");
    myExt = Arguments[0];
    PathFolderInput = Arguments[1];
    myAnalysis = Arguments[2];
    myCommands = Arguments[3];
    myReuse = Arguments[4];

    //Find all files and store path in myAnalysis
    listFiles(PathFolderInput, myExt, myAnalysis);

    //Retrieve number of files
    RawList = File.openAsString(myAnalysis);
    FileList = split(RawList, "\n");

    if (FileList.length>0){
        //Inform user
        if (myReuse == "NO"){
            DisplayInfo("<b>" + FileList.length + "</b> files have been found.<br>"
                        + "Press <b>OK</b> when ready for manual pre-processing.");
        }else{
            DisplayInfo("<b>" + FileList.length + "</b> files have been found.<br>"
                        + "Press <b>OK</b> when ready to precise which parameters to reuse.");
        }

    }else{
        //Inform user
        DisplayInfo("<b>" + FileList.length + "</b> files have been found.<br>"
                    + "<b>The process will be aborted</b>.");
        de = File.delete(myAnalysis);
        de = File.delete(myCommands);
        run("Quit");
    }

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

function DisplayInfo(Message){
    showMessage("", "<html>"
			+"<font size=+3>"
			+"<h1><font color=rgb(77,172,174)>Lipid Droplets Analysis</h1>"
			+"<font size=+0>"
			+"<font color=rgb(0,0,0)>"
            +"<p>" + Message + "</p>"
			);
}//END DisplayInfo

}

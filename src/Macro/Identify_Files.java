macro "Identify_Files"{

    /*
    Identify the files with the correct extension and store the paths
    in myAnalysis.
    */

    //Retrieve arguments into an array
    Argument = getArgument();
    Arguments = split(Argument, "*");

    //Extension of the wanted files
    myExt = Arguments[0];

    //Path of the root folder
    PathFolderInput = Arguments[1];

    //Path of the txt file that will contain
    //all the paths of the files to be analyzed
    myAnalysis = Arguments[2];

    //Path of the txt file that will contain
    //all the parameters of analysis for each identified files
    myCommands = Arguments[3];

    //Reuse old parameters or not
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

        //As no files are found, the parameter and listing files
        //are deleted and the program shut IJ down
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

    //Search files with the correct extension in a root folder and
    //its subfolders.

    //Start the search
	list = getFileList(folder);
	for (i=0; i<list.length; i++) {

        //If it is a folder the program search in it
        if (File.isDirectory(folder+list[i])){
           	listFiles(""+folder+list[i], extension, outFilePath);
       	}

		if (endsWith(list[i], extension)){
            //Only file with the correct extension is added to the file
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
			+"<h1><font color=rgb(77,172,174)>Automated Detection of labeled particles <br> in microscopic stacks of Drosophila M. tissues</h1>"
			+"<font size=+0>"
			+"<font color=rgb(0,0,0)>"
            +"<p>" + Message + "</p>"
			);
}//END DisplayInfo

}

macro "AjoutPlugin" {

    //Pathof the ImageJ folder
    pathImagej = getDirectory("imagej");

    //Name of the plugin to find
    Nameplugin="Animated_Gif.jar";

    //Search the plugin in ImageJ folder
    Resultat = findPlugin(pathImagej, Nameplugin);

    //Inform the user
    if(Resultat==0){
    	DisplayInfo("Plugin "+Nameplugin+" is missing. It will be automaticaly installed.");
        return "missing";
    }else{
    	DisplayInfo("Plugin "+Nameplugin+" is installed.");
        return "here";
    }


/*
===============================================================================
                            FUNCTIONS
===============================================================================
*/

function findPlugin(logicielpath, PluginName) {

    //Initialize the variable of detection 
	myres = 0;

    //Start the search
	list = getFileList(logicielpath);
	for (i=0; i<list.length; i++) {

       	if (File.isDirectory(logicielpath+list[i])){
           		myres= findPlugin(""+logicielpath+list[i], PluginName);
			if (myres==1){
				i=list.length+1000; //exit
			}
       	}else{
			if(list[i]==PluginName){
				myres=1;
				i=list.length+1000; //exit
			}
        }
	}
    //Return if the file is found or not
	return myres;
}

/*
================================================================================
*/

function DisplayInfo(Message){
    showMessage("", "<html>"
			+"<font size=+3>"
			+"<h1><font color=rgb(77,172,174)>Installation tool</h1>"
			+"<font size=+0>"
			+"<font color=rgb(0,0,0)>"
            +"<p>" + Message + "</p>"
			);
}//END DisplayInfo


}
//End of the macro

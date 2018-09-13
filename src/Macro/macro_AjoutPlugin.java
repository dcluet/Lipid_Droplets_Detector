macro "AjoutPlugin" {


pathImagej=getDirectory("imagej");
Nameplugin="Animated_Gif.jar";
Resultat = findPlugin(pathImagej, Nameplugin);

if(Resultat==0){
	DisplayInfo("Plugin "+Nameplugin+" is missing. It will be automaticaly installed.");
    return "missing";
}else{
	DisplayInfo("Plugin "+Nameplugin+" is installed.");
    return "here";
}

//FUNCTIONS________________________________________________________________________________________________________

function findPlugin(logicielpath, PluginName) {

	myres = 0;
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





























}

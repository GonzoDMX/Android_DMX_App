package com.example.android_dmx_remote


object ModelCommandRegex {

    //Regex check for validating the syntax of the command line input
    fun cmdLineCheck(input: String): Boolean{
        val regCmdLine = Regex("^(((?:51[0-2]|50[0-9]|[1-4][0-9][0-9]|[1-9][0-9]|[1-9])" +
                "(<(?![-+@])|<51[0-2]|<50[0-9]|<[1-4][0-9][0-9]|<[1-9][0-9]|<[1-9])?)|(?:ALL))" +
                "(([+-](?![+-@])|[+-]51[0-2]|[+-]50[0-9]|[+-][1-4][0-9][0-9]|[+-][1-9][0-9]|" +
                "[+-][1-9])(<(?![@+-])|<51[0-2]|<50[0-9]|<[1-4][0-9][0-9]|<[1-9][0-9]|<[1-9])?)" +
                "*((@(?![t])|@25[0-5]|@2[0-4][0-9]|@1[0-9][0-9]|@[1-9][0-9]|@[0-9])((t|t[1-9]" +
                "[0-9]|t[0-9])([.]|[.][1-9])?)?)?$")

        return regCmdLine.matches(input)
    }

    //Splits channel string into a list of channel numbers and modifiers ( < + - )
    fun splitChannels(input: String): List<String> {
        return input.split(regex = Regex("((?<=[<+-])|(?=[<+-]))"))
    }
}
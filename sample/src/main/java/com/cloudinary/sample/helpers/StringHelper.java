package com.cloudinary.sample.helpers;

public class StringHelper {

    public static String captialLetter(String input) {

// Capitalize the first letter

// Set the capitalized text to the TextView
        return Character.toUpperCase(input.charAt(0)) + input.substring(1);
    }
}

/*
 * Copyright (c) 2021. Lukas Jonsson
 */

package de.verdox.vcore.impl.gameserver.paper.utils.util;

import org.bukkit.ChatColor;
import org.bukkit.map.MinecraftFont;

import java.util.ArrayList;
import java.util.List;


public class BukkitBookUtil {
    public static List<String> getLines(String rawText) {
        //Note that the only flaw with using MinecraftFont is that it can't account for some UTF-8 symbols, it will throw an IllegalArgumentException
        final MinecraftFont font = new MinecraftFont();
        final int maxLineWidth = font.getWidth("LLLLLLLLLLLLLLLLLLL");

        //Get all of our lines
        List<String> lines = new ArrayList<>();
        try {
            //Each 'section' is separated by a line break (\n)
            for (String section : rawText.split("\n")) {
                //If the section is blank, that means we had a double line break there
                if (section.equals(""))
                    lines.add("(BREAK)");
                    //We have an actual section with some content
                else {
                    //Iterate through all the words of the section
                    String[] words = ChatColor.stripColor(section).split(" ");
                    String line = "";
                    for (int index = 0; index < words.length; index++) {
                        String word = words[index];
                        //Make sure we can actually use this next word in our current line
                        String test = (line + " " + word);
                        if (test.startsWith(" ")) test = test.substring(1);
                        //Current line + word is too long to be one line
                        if (font.getWidth(test) > maxLineWidth) {
                            //Add our current line
                            lines.add(line);
                            //Set our next line to start off with this word
                            line = word;
                            continue;
                        }
                        //Add the current word to our current line
                        line = test;
                    }
                    //Make sure we add the line if it was the last word and wasn't too long for the line to start a new one
                    if (!line.equals(""))
                        lines.add(line);
                }
            }
        } catch (IllegalArgumentException ex) {
            lines.clear();
        }

        return lines;
    }

    public static String rawButtonFormat(String rawText) {
        final MinecraftFont font = new MinecraftFont();
        final int maxLineWidth = font.getWidth("LLLLLLLLLLLLLLLLLLL");
        final int textWidth = font.getWidth(rawText);
        final int spaceWidth = font.getWidth(" ");
        final int paddingSize = maxLineWidth - textWidth - (font.getWidth("[") * 2);
        final int spaceCount = paddingSize / spaceWidth;
        int leftSide = spaceCount / 2;
        int rightSite = spaceCount - leftSide;

        StringBuilder stringBuilder = new StringBuilder("[");

        int leftCounter = 0;
        while (leftCounter < leftSide - 3) {
            stringBuilder.append(" ");
            leftCounter++;
        }
        stringBuilder.append(rawText);
        int rightCounter = 0;
        while (rightCounter < rightSite - 3) {
            stringBuilder.append(" ");
            rightCounter++;
        }
        stringBuilder.append("]");

        // Leading §r to prevent the string trimming
        return stringBuilder.toString();
    }
}

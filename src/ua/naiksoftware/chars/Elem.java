/**
 * Game create for competition on http://annimon.com
 *
 * Copyright (C) 2012, 2013 NaikSoftware
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package ua.naiksoftware.chars;

public class Elem {

    public int x;
    public int y;
    public char ch;

    // TODO: убрать публичный доступ к полям?
    public Elem(char symbol, int x, int y) {
        this.x = x;
        this.y = y;
        this.ch = symbol;
    }

    public void move(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    public char getChar() {
        return this.ch;
    }

    public void setChar(char ch) {
        this.ch = ch;
    }
}

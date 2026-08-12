package v.akfz.aslib.render.gui.widget.impl.text.helper;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;

public class TextBinds {

    public static class TextState {
        private String text;
        private int cursorPos;
        private int selStart;
        private int selEnd;

        public TextState(String text, int cursorPos, int selStart, int selEnd) {
            this.text = text;
            this.cursorPos = cursorPos;
            this.selStart = selStart;
            this.selEnd = selEnd;
        }

        public String getText() { return text; }
        public void setText(String text) { this.text = text; }

        public int getCursorPos() { return cursorPos; }
        public void setCursorPos(int cursorPos) { this.cursorPos = cursorPos; }

        public int getSelStart() { return selStart; }
        public void setSelStart(int selStart) { this.selStart = selStart; }

        public int getSelEnd() { return selEnd; }
        public void setSelEnd(int selEnd) { this.selEnd = selEnd; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            TextState textState = (TextState) o;
            return cursorPos == textState.cursorPos &&
                    selStart == textState.selStart &&
                    selEnd == textState.selEnd &&
                    Objects.equals(text, textState.text);
        }

        @Override
        public int hashCode() {
            return Objects.hash(text, cursorPos, selStart, selEnd);
        }

        @Override
        public String toString() {
            return "TextState{" +
                    "text='" + text + '\'' +
                    ", cursorPos=" + cursorPos +
                    ", selStart=" + selStart +
                    ", selEnd=" + selEnd +
                    '}';
        }
    }

    private final Deque<TextState> undoStack = new ArrayDeque<>();
    private final Deque<TextState> redoStack = new ArrayDeque<>();

    public void saveState(TextState current) {
        undoStack.push(new TextState(current.text, current.cursorPos, current.selStart, current.selEnd));
        redoStack.clear();
    }

    public TextState undo(TextState current) {
        if (undoStack.isEmpty())
            return null;
        redoStack.push(current);
        return undoStack.pop();
    }

    public TextState redo(TextState current) {
        if (redoStack.isEmpty())
            return null;
        undoStack.push(current);
        return redoStack.pop();
    }

    private void applyState(TextState state, StringBuilder text, int[] cursorPos, int[] selStart, int[] selEnd) {
        text.setLength(0);
        text.append(state.text);
        cursorPos[0] = state.cursorPos;
        selStart[0] = state.selStart;
        selEnd[0] = state.selEnd;
    }

    private TextState currentState(StringBuilder text, int[] cursorPos, int[] selStart, int[] selEnd) {
        return new TextState(text.toString(), cursorPos[0], selStart[0], selEnd[0]);
    }

    public boolean handleKeyPress(int keyCode, int modifiers,
                                  StringBuilder text,
                                  int[] cursorPos,
                                  int[] selStart,
                                  int[] selEnd,
                                  boolean multiline) {
        boolean ctrl = (modifiers & 2) != 0;
        boolean shift = (modifiers & 1) != 0;

        if (ctrl) {
            switch (keyCode) {
                case 67: // C
                    copy(text, cursorPos, selStart, selEnd);
                    return true;
                case 86: // V
                    paste(text, cursorPos, selStart, selEnd);
                    return true;
                case 88: // X
                    cut(text, cursorPos, selStart, selEnd);
                    return true;
                case 65: // A
                    selectAll(text, cursorPos, selStart, selEnd);
                    return true;
                case 90: // Z
                    if (shift) {
                        // Ctrl+Shift+Z
                        TextState redoState = redo(currentState(text, cursorPos, selStart, selEnd));
                        if (redoState != null) {
                            applyState(redoState, text, cursorPos, selStart, selEnd);
                            return true;
                        }
                    } else {
                        // Ctrl+Z
                        TextState undoState = undo(currentState(text, cursorPos, selStart, selEnd));
                        if (undoState != null) {
                            applyState(undoState, text, cursorPos, selStart, selEnd);
                            return true;
                        }
                    }
                    return false;
                case 89: // Y
                    TextState redoState = redo(currentState(text, cursorPos, selStart, selEnd));
                    if (redoState != null) {
                        applyState(redoState, text, cursorPos, selStart, selEnd);
                        return true;
                    }
                    return false;
            }
        }

        switch (keyCode) {
            case 263: // LEFT
                handleCursorMove(text, cursorPos, selStart, selEnd, shift, -1, ctrl);
                return true;
            case 262: // RIGHT
                handleCursorMove(text, cursorPos, selStart, selEnd, shift, 1, ctrl);
                return true;
            case 268: // HOME
                handleCursorMove(text, cursorPos, selStart, selEnd, shift, -2, ctrl);
                return true;
            case 269: // END
                handleCursorMove(text, cursorPos, selStart, selEnd, shift, 2, ctrl);
                return true;
            case 259: // BACKSPACE
                handleBackspace(text, cursorPos, selStart, selEnd);
                return true;
            case 261: // DELETE
                handleDelete(text, cursorPos, selStart, selEnd);
                return true;
            case 257: // ENTER
            case 335: // NUMPAD ENTER
                if (multiline) {
                    handleEnter(text, cursorPos, selStart, selEnd);
                    return true;
                }
                return false;
        }
        return false;
    }

    public boolean handleCharTyped(char chr, int modifiers,
                                   StringBuilder text,
                                   int[] cursorPos,
                                   int[] selStart,
                                   int[] selEnd) {
        if (chr >= ' ' && chr != 127) {
            if (hasSelection(selStart, selEnd)) {
                saveState(currentState(text, cursorPos, selStart, selEnd));
            }

            if (hasSelection(selStart, selEnd)) {
                deleteSelection(text, cursorPos, selStart, selEnd);
            }

            text.insert(cursorPos[0], chr);
            cursorPos[0]++;

            if (Character.isWhitespace(chr)) {
                saveState(currentState(text, cursorPos, selStart, selEnd));
            }

            clearSelection(selStart, selEnd);
            return true;
        }
        return false;
    }

    public static int getCharIndexAt(String text, int mouseX, int textX, int scrollOffset) {
        Font font = Minecraft.getInstance().font;
        int relativeX = mouseX - textX + scrollOffset;
        if (relativeX <= 0)
            return 0;
        for (int i = 1; i <= text.length(); i++) {
            String prefix = text.substring(0, i);
            if (font.width(prefix) > relativeX) {
                return i - 1;
            }
        }
        return text.length();
    }

    private static boolean hasSelection(int[] selStart, int[] selEnd) {
        return selStart[0] != -1 && selEnd[0] != -1 && selStart[0] != selEnd[0];
    }

    private static void clearSelection(int[] selStart, int[] selEnd) {
        selStart[0] = -1;
        selEnd[0] = -1;
    }

    private void handleCursorMove(StringBuilder text, int[] cursorPos, int[] selStart, int[] selEnd,
                                  boolean shift, int direction, boolean word) {
        if (shift) {
            if (selStart[0] == -1) {
                selStart[0] = cursorPos[0];
                selEnd[0] = cursorPos[0];
            }
        }

        switch (direction) {
            case -1: // left
                moveCursorLeft(text, cursorPos, word);
                break;
            case 1: // right
                moveCursorRight(text, cursorPos, word);
                break;
            case -2: // home
                cursorPos[0] = 0;
                break;
            case 2: // end
                cursorPos[0] = text.length();
                break;
        }

        if (shift) {
            selEnd[0] = cursorPos[0];
        } else {
            clearSelection(selStart, selEnd);
        }
    }

    private static void moveCursorLeft(StringBuilder text, int[] cursorPos, boolean word) {
        if (cursorPos[0] > 0) {
            if (word) {
                int i = cursorPos[0] - 1;
                while (i > 0 && text.charAt(i - 1) != ' ')
                    i--;
                cursorPos[0] = i;
            } else {
                cursorPos[0]--;
            }
        }
    }

    private static void moveCursorRight(StringBuilder text, int[] cursorPos, boolean word) {
        if (cursorPos[0] < text.length()) {
            if (word) {
                int i = cursorPos[0] + 1;
                while (i < text.length() && text.charAt(i - 1) != ' ')
                    i++;
                cursorPos[0] = i;
            } else {
                cursorPos[0]++;
            }
        }
    }

    private void handleBackspace(StringBuilder text, int[] cursorPos, int[] selStart, int[] selEnd) {
        saveState(currentState(text, cursorPos, selStart, selEnd));
        if (hasSelection(selStart, selEnd)) {
            deleteSelection(text, cursorPos, selStart, selEnd);
        } else if (cursorPos[0] > 0) {
            text.deleteCharAt(cursorPos[0] - 1);
            cursorPos[0]--;
        }
    }

    private void handleDelete(StringBuilder text, int[] cursorPos, int[] selStart, int[] selEnd) {
        saveState(currentState(text, cursorPos, selStart, selEnd));
        if (hasSelection(selStart, selEnd)) {
            deleteSelection(text, cursorPos, selStart, selEnd);
        } else if (cursorPos[0] < text.length()) {
            text.deleteCharAt(cursorPos[0]);
        }
    }

    private void handleEnter(StringBuilder text, int[] cursorPos, int[] selStart, int[] selEnd) {
        saveState(currentState(text, cursorPos, selStart, selEnd));
        if (hasSelection(selStart, selEnd)) {
            deleteSelection(text, cursorPos, selStart, selEnd);
        }
        text.insert(cursorPos[0], '\n');
        cursorPos[0]++;
        clearSelection(selStart, selEnd);
    }

    private void deleteSelection(StringBuilder text, int[] cursorPos, int[] selStart, int[] selEnd) {
        int start = Math.min(selStart[0], selEnd[0]);
        int end = Math.max(selStart[0], selEnd[0]);
        text.delete(start, end);
        cursorPos[0] = start;
        clearSelection(selStart, selEnd);
    }

    private void copy(StringBuilder text, int[] cursorPos, int[] selStart, int[] selEnd) {
        if (hasSelection(selStart, selEnd)) {
            int start = Math.min(selStart[0], selEnd[0]);
            int end = Math.max(selStart[0], selEnd[0]);
            String selected = text.substring(start, end);
            Minecraft.getInstance().keyboardHandler.setClipboard(selected);
        }
    }

    private void cut(StringBuilder text, int[] cursorPos, int[] selStart, int[] selEnd) {
        if (hasSelection(selStart, selEnd)) {
            copy(text, cursorPos, selStart, selEnd);
            saveState(currentState(text, cursorPos, selStart, selEnd));
            deleteSelection(text, cursorPos, selStart, selEnd);
        }
    }

    private void paste(StringBuilder text, int[] cursorPos, int[] selStart, int[] selEnd) {
        String clipboard = Minecraft.getInstance().keyboardHandler.getClipboard();
        if (clipboard == null || clipboard.isEmpty())
            return;

        saveState(currentState(text, cursorPos, selStart, selEnd));

        if (hasSelection(selStart, selEnd)) {
            deleteSelection(text, cursorPos, selStart, selEnd);
        }

        text.insert(cursorPos[0], clipboard);
        cursorPos[0] += clipboard.length();
        clearSelection(selStart, selEnd);
    }

    private void selectAll(StringBuilder text, int[] cursorPos, int[] selStart, int[] selEnd) {
        selStart[0] = 0;
        selEnd[0] = text.length();
        cursorPos[0] = text.length();
    }
}
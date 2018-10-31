/**
 * Copyright (C) 2017 VMarch
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

var URE = {};

URE.currentSelection = {
    "startContainer": 0,
    "startOffset": 0,
    "endContainer": 0,
    "endOffset": 0};

URE.editor = document.getElementById('editor');

document.addEventListener("selectionchange", function() { URE.backuprange(); });

URE.setHtml = function(contents) {
   URE.editor.innerHTML = decodeURIComponent(contents.replace(/\+/g, '%20'));
}

URE.getHtml = function() {
    return URE.editor.innerHTML;
}

URE.getText = function() {
    return URE.editor.innerText;
}

URE.backComplexInfo = function() {
    var items = [];
    if (document.queryCommandState('bold')) {
        items.push('bold');
    }
    if (document.queryCommandState('italic')) {
        items.push('italic');
    }
       if (document.queryCommandState('underline')) {
        items.push('underline');
    }
    if (document.queryCommandState('insertOrderedList')) {
        items.push('orderedList');
    }
    if (document.queryCommandState('insertUnorderedList')) {
        items.push('unorderedList');
    }
    var formatBlock = document.queryCommandValue('formatBlock');
    if (formatBlock.length > 0) {
        items.push(formatBlock);
    }

    window.location.href = "style-part://" + encodeURI(items.join(',')) + ',text-part://' + encodeURI(URE.getHtml());
}

URE.setBaseTextColor = function(color) {
    URE.editor.style.color  = color;
}

URE.setBaseFontSize = function(size) {
    URE.editor.style.fontSize = size;
}

URE.setPadding = function(left, top, right, bottom) {
    URE.editor.style.paddingLeft = left;
    URE.editor.style.paddingTop = top;
    URE.editor.style.paddingRight = right;
    URE.editor.style.paddingBottom = bottom;
}

URE.setBackgroundColor = function(color) {
    document.body.style.backgroundColor = color;
}

URE.setBackgroundImage = function(image) {
    URE.editor.style.backgroundImage = image;
}

URE.setWidth = function(size) {
    URE.editor.style.minWidth = size;
}

URE.setHeight = function(size) {
    URE.editor.style.height = size;
}

URE.setTextAlign = function(align) {
    URE.editor.style.textAlign = align;
}

URE.setVerticalAlign = function(align) {
    URE.editor.style.verticalAlign = align;
}

URE.setPlaceholder = function(placeholder) {
    URE.editor.setAttribute("placeholder", placeholder);
}

URE.setInputEnabled = function(inputEnabled) {
    URE.editor.contentEditable = String(inputEnabled);
}

URE.undo = function() {
    document.execCommand('undo', false, null);
}

URE.setBold = function() {
    document.execCommand('bold', false, null);
}

URE.setItalic = function() {
    document.execCommand('italic', false, null);
}

URE.setUnderline = function() {
    document.execCommand('underline', false, null);
}

URE.setBullets = function() {
    document.execCommand('insertUnorderedList', false, null);
}

URE.setNumbers = function() {
    document.execCommand('insertOrderedList', false, null);
}

URE.setTextColor = function(color) {
    URE.restorerange();
    document.execCommand("styleWithCSS", null, true);
    document.execCommand('foreColor', false, color);
    document.execCommand("styleWithCSS", null, false);
}

URE.setFontSize = function(fontSize){
    document.execCommand("fontSize", false, fontSize);
}

URE.insertImage = function(url, alt) {
    var html = '<img src="' + url + '" alt="' + alt + '" />';
    URE.insertHTML(html);
}

URE.insertStyledImage = function(url, styles, alt) {
    var html = '<img src="' + url + '" style="' + styles  + '" alt="' + alt + '" />';
    URE.insertHTML(html);
}

URE.insertHTML = function(html) {
    URE.restorerange();
    document.execCommand('insertHTML', false, html);
}

URE.insertLink = function(url, title) {
    URE.restorerange();
    var sel = document.getSelection();
    if (sel.toString().length == 0) {
        document.execCommand("insertHTML",false,"<a href='"+url+"'>"+title+"</a>");
    } else if (sel.rangeCount) {
       var el = document.createElement("a");
       el.setAttribute("href", url);
       el.setAttribute("title", title);

       var range = sel.getRangeAt(0).cloneRange();
       range.surroundContents(el);
       sel.removeAllRanges();
       sel.addRange(range);
    }
    URE.backComplexInfo();
}

URE.prepareInsert = function() {
    URE.backuprange();
}

URE.backuprange = function(){
    var selection = window.getSelection();
    if (selection.rangeCount > 0) {
      var range = selection.getRangeAt(0);
      URE.currentSelection = {
          "startContainer": range.startContainer,
          "startOffset": range.startOffset,
          "endContainer": range.endContainer,
          "endOffset": range.endOffset};
    }
}

URE.restorerange = function(){
    var selection = window.getSelection();
    selection.removeAllRanges();
    var range = document.createRange();
    range.setStart(URE.currentSelection.startContainer, URE.currentSelection.startOffset);
    range.setEnd(URE.currentSelection.endContainer, URE.currentSelection.endOffset);
    selection.addRange(range);
}

URE.focus = function() {
    var range = document.createRange();
    range.selectNodeContents(URE.editor);
    range.collapse(false);
    var selection = window.getSelection();
    selection.removeAllRanges();
    selection.addRange(range);
    URE.editor.focus();
}

URE.blurFocus = function() {
    URE.editor.blur();
}

// Event Listeners
URE.editor.addEventListener("input", URE.backComplexInfo);
URE.editor.addEventListener("keyup", function(e) {
    var KEY_LEFT = 37, KEY_RIGHT = 39;
    if (e.which == KEY_LEFT || e.which == KEY_RIGHT) {
        URE.enabledEditingItems(e);
    }
});
URE.editor.addEventListener("click", URE.backComplexInfo);


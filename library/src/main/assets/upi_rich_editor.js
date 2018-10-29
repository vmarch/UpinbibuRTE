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

var RTE = {};

RTE.currentSelection = {
    "startContainer": 0,
    "startOffset": 0,
    "endContainer": 0,
    "endOffset": 0};

RTE.editor = document.getElementById('editor');

document.addEventListener("selectionchange", function() { RTE.backuprange(); });

RTE.callback = function() {
    window.location.href = "rte-callback://" + encodeURI(RTE.getHtml());
}

RTE.setHtml = function(contents) {
    RTE.editor.innerHTML = decodeURIComponent(contents.replace(/\+/g, '%20'));
}

RTE.getHtml = function() {
    return RTE.editor.innerHTML;
}

RTE.getText = function() {
    return RTE.editor.innerText;
}

RTE.setBaseTextColor = function(color) {
    RTE.editor.style.color  = color;
}

RTE.setBaseFontSize = function(size) {
    RTE.editor.style.fontSize = size;
}

RTE.setPadding = function(left, top, right, bottom) {
  RTE.editor.style.paddingLeft = left;
  RTE.editor.style.paddingTop = top;
  RTE.editor.style.paddingRight = right;
  RTE.editor.style.paddingBottom = bottom;
}

RTE.setBackgroundColor = function(color) {
    document.body.style.backgroundColor = color;
}

RTE.setBackgroundImage = function(image) {
    RTE.editor.style.backgroundImage = image;
}

RTE.setWidth = function(size) {
    RTE.editor.style.minWidth = size;
}

RTE.setHeight = function(size) {
    RTE.editor.style.height = size;
}

RTE.setTextAlign = function(align) {
    RTE.editor.style.textAlign = align;
}

RTE.setVerticalAlign = function(align) {
    RTE.editor.style.verticalAlign = align;
}

RTE.setPlaceholder = function(placeholder) {
    RTE.editor.setAttribute("placeholder", placeholder);
}

RTE.setInputEnabled = function(inputEnabled) {
    RTE.editor.contentEditable = String(inputEnabled);
}

RTE.undo = function() {
    document.execCommand('undo', false, null);
}

RTE.redo = function() {
    document.execCommand('redo', false, null);
}

RTE.setBold = function() {
    document.execCommand('bold', false, null);
}

RTE.setItalic = function() {
    document.execCommand('italic', false, null);
}

RTE.setUnderline = function() {
    document.execCommand('underline', false, null);
}

RTE.setBullets = function() {
    document.execCommand('insertUnorderedList', false, null);
}

RTE.setNumbers = function() {
    document.execCommand('insertOrderedList', false, null);
}

RTE.setTextColor = function(color) {
    RTE.restorerange();
    document.execCommand("styleWithCSS", null, true);
    document.execCommand('foreColor', false, color);
    document.execCommand("styleWithCSS", null, false);
}

RTE.setTextBackgroundColor = function(color) {
    RTE.restorerange();
    document.execCommand("styleWithCSS", null, true);
    document.execCommand('hiliteColor', false, color);
    document.execCommand("styleWithCSS", null, false);
}

RTE.setFontSize = function(fontSize){
    document.execCommand("fontSize", false, fontSize);
}

RTE.setHeading = function(heading) {
    document.execCommand('formatBlock', false, '<h'+heading+'>');
}

RTE.setIndent = function() {
    document.execCommand('indent', false, null);
}

RTE.setOutdent = function() {
    document.execCommand('outdent', false, null);
}

RTE.setJustifyLeft = function() {
    document.execCommand('justifyLeft', false, null);
}

RTE.setJustifyCenter = function() {
    document.execCommand('justifyCenter', false, null);
}

RTE.setJustifyRight = function() {
    document.execCommand('justifyRight', false, null);
}

RTE.setBlockquote = function() {
    document.execCommand('formatBlock', false, '<blockquote>');
}

RTE.insertImage = function(url, alt) {
    var html = '<img src="' + url + '" alt="' + alt + '" />';
    RTE.insertHTML(html);
}

RTE.insertHTML = function(html) {
    RTE.restorerange();
    document.execCommand('insertHTML', false, html);
}

RTE.insertLink = function(url, title) {
    RTE.restorerange();
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
    RTE.callback();
}

RTE.prepareInsert = function() {
    RTE.backuprange();
}

RTE.backuprange = function(){
    var selection = window.getSelection();
    if (selection.rangeCount > 0) {
      var range = selection.getRangeAt(0);
      RTE.currentSelection = {
          "startContainer": range.startContainer,
          "startOffset": range.startOffset,
          "endContainer": range.endContainer,
          "endOffset": range.endOffset};
    }
}

RTE.restorerange = function(){
    var selection = window.getSelection();
    selection.removeAllRanges();
    var range = document.createRange();
    range.setStart(RTE.currentSelection.startContainer, RTE.currentSelection.startOffset);
    range.setEnd(RTE.currentSelection.endContainer, RTE.currentSelection.endOffset);
    selection.addRange(range);
}

RTE.enabledEditingItems = function(e) {
    var items = [];
    if (document.queryCommandState('bold')) {
        items.push('bold');
    }
    if (document.queryCommandState('italic')) {
        items.push('italic');
    }
    if (document.queryCommandState('subscript')) {
        items.push('subscript');
    }
    if (document.queryCommandState('superscript')) {
        items.push('superscript');
    }
    if (document.queryCommandState('strikeThrough')) {
        items.push('strikeThrough');
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
    if (document.queryCommandState('justifyCenter')) {
        items.push('justifyCenter');
    }
    if (document.queryCommandState('justifyFull')) {
        items.push('justifyFull');
    }
    if (document.queryCommandState('justifyLeft')) {
        items.push('justifyLeft');
    }
    if (document.queryCommandState('justifyRight')) {
        items.push('justifyRight');
    }
    if (document.queryCommandState('insertHorizontalRule')) {
        items.push('horizontalRule');
    }
    var formatBlock = document.queryCommandValue('formatBlock');
    if (formatBlock.length > 0) {
        items.push(formatBlock);
    }

    window.location.href = "rte-state://" + encodeURI(items.join(','));
}

RTE.focus = function() {
    var range = document.createRange();
    range.selectNodeContents(RTE.editor);
    range.collapse(false);
    var selection = window.getSelection();
    selection.removeAllRanges();
    selection.addRange(range);
    RTE.editor.focus();
}

RTE.blurFocus = function() {
    RTE.editor.blur();
}

RTE.removeFormat = function() {
    document.execCommand('removeFormat', false, null);
}

// Event Listeners
RTE.editor.addEventListener("input", RTE.callback);

//RTE.editor.addEventListener("keyup", function(e) {
//    var KEY_LEFT = 37, KEY_RIGHT = 39;
//    if (e.which == KEY_LEFT || e.which == KEY_RIGHT) {
//        RTE.enabledEditingItems(e);
//    }
//});
RTE.editor.addEventListener("click", RTE.enabledEditingItems);
//RTE.editor.addEventListener("keyup", RTE.enabledEditingItems);

/*
  Licensed to the Apache Software Foundation (ASF) under one or more
  contributor license agreements.  See the NOTICE file distributed with
  this work for additional information regarding copyright ownership.
  The ASF licenses this file to You under the Apache License, Version 2.0
  (the "License"); you may not use this file except in compliance with
  the License.  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
*/
function iframePortlet_resetHeight(iframe) {
  try {
    if (iframe.contentDocument && iframe.contentDocument.body.offsetHeight) {
      var offsetHeight = iframe.contentDocument.body.offsetHeight;
      if (iframe.contentDocument.body.scrollHeight && iframe.contentDocument.body.scrollHeight > offsetHeight)
        offsetHeight = iframe.contentDocument.body.scrollHeight;
      iframe.height = offsetHeight + 16;
    } else if (iframe.Document && iframe.Document.body.scrollHeight) {
      iframe.height = iframe.Document.body.scrollHeight;
    }
  } catch (e) {
  }
}
function iframePortlet_onreadystatechange() {
}
function iframePortlet_recordVisitPage(iframe) {
  try {
    var xmlHttp = (window.XMLHttpRequest ? new XMLHttpRequest() : new ActiveXObject("Microsoft.XMLHTTP"));
    var visitedPage = "" + iframe.contentWindow.location.href;
    if (window.location.href.match(/^(https?:\/\/[^\/]+)\/?/)) {
      var baseURL = "" + RegExp.$1;
      if (visitedPage.indexOf(baseURL) == 0) {
        visitedPage = visitedPage.substring(baseURL.length);
      }
    }
    var visitResourceURL = "" + iframe.getAttribute("visitresourceurl");
    visitResourceURL += (visitResourceURL.indexOf("?") > 0 ? "&" : "?");
    visitResourceURL += ("URL=" + encodeURIComponent(visitedPage));
    xmlHttp.open("GET", visitResourceURL, true);
    xmlHttp.onreadystatechange = iframePortlet_onreadystatechange;
    xmlHttp.send(null);
  } catch (e) {
  }
}
function iframePortlet_attachEvents(iframe) {
  try {
    if (window.addEventListener) {
      iframe.addEventListener("load", iframePortlet_iframeOnLoad, false);
    } else if (window.attachEvent) {
      iframe.detachEvent("onload", iframePortlet_iframeOnLoad);;
      iframe.attachEvent("onload", iframePortlet_iframeOnLoad);
    }
  } catch (e) {
  }
}
function iframePortlet_iframeOnLoad(evt) {
  var iframe = (window.event ? window.event.srcElement : evt.currentTarget);
  if (iframe) {
    var autoResize = "" + iframe.getAttribute("autoresize");
    if (autoResize.match(/^(true)|(yes)|(on)$/i)) {
      iframePortlet_resetHeight(iframe);
    }
    var visitLastPage = "" + iframe.getAttribute("visitlastpage");
    if (visitLastPage.match(/^(true)|(yes)|(on)$/i)) {
        iframePortlet_recordVisitPage(iframe);
    }
  }
}
var iframePortlet_iframesContainerOnLoad_working = false;
function iframePortlet_iframesContainerOnLoad() {
  if (iframePortlet_iframesContainerOnLoad_working) return;
  iframePortlet_iframesContainerOnLoad_working = true;
  var iframes = document.getElementsByTagName("IFRAME");
  for (var i = 0; i < iframes.length; i++) {
    var autoResize = "" + iframes[i].getAttribute("autoresize");
    if (autoResize.match(/^(true)|(yes)|(on)$/i)) {
      iframePortlet_resetHeight(iframes[i]);
    }
    var visitLastPage = "" + iframes[i].getAttribute("visitlastpage");
    if (visitLastPage.match(/^(true)|(yes)|(on)$/i)) {
        iframePortlet_recordVisitPage(iframes[i]);
    }
    iframePortlet_attachEvents(iframes[i]);
  }
  iframePortlet_iframesContainerOnLoad_working = false;
}
if (window.addEventListener) {
  window.addEventListener("load", iframePortlet_iframesContainerOnLoad, false);
} else if (window.attachEvent) {
  window.attachEvent("onload", iframePortlet_iframesContainerOnLoad)
}

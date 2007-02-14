<?xml version="1.0"?>
<!--
    [[
    Copyright (C) 2006 The Software Conservancy as Trustee. All rights
    reserved.

    Permission is hereby granted, free of charge, to any person obtaining a
    copy of this software and associated documentation files (the
    "Software"), to deal in the Software without restriction, including
    without limitation the rights to use, copy, modify, merge, publish,
    distribute, sublicense, and/or sell copies of the Software, and to
    permit persons to whom the Software is furnished to do so, subject to
    the following conditions:

    The above copyright notice and this permission notice shall be included
    in all copies or substantial portions of the Software.

    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
    OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
    MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
    NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
    LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
    OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
    WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

    Nothing in this notice shall be deemed to grant any rights to
    trademarks, copyrights, patents, trade secrets or any other intellectual
    property of the licensor or any contributor except as expressly stated
    herein. No patent license is granted separate from the Software, for
    code that you delete from the Software, or for combinations of the
    Software with other software or hardware.
    ]]

    $Header: /u1/sourcecast/data/ccvs/repository/oa3/cookbook/xslt/nodemap.xsl,v 1.7 2006/10/10 13:09:22 shirea Exp $

    @author Andrew Shire

    Produces a single HTML page containing a single cookbook example as
    embedded clickable node map images.
    This is used to generate a separate page for each cookbook example.

    Related to "allnodemaps.xsl" which produces same output except that
    every cookbook example appears on a single page.
    This is used to generate cookbook/images/index.html.
  -->
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns="http://www.w3.org/TR/xhtml1/strict"
                xmlns:beans="http://www.springframework.org/schema/beans">

<xsl:param name="oaVersion"/>
<xsl:param name="imageFileExtension"/>

<xsl:template match="/">

<xsl:variable name="thisExample" select="substring-before(substring-after(beans:beans/beans:description|comment(),'HeadURL: https://www.openadaptor.org/svn/openadaptor3/trunk/example/spring/'),'.xml ')"/>

<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=iso-8859-15"/>
    <style type="text/css">
        html { background: white }
        body {
            background: white;
            color: black;
            font-family: Arial, Helvetica, san-serif
        }
        td { text-align: left; vertical-align: top }
        th { text-align: left; vertical-align: top }
        a.th:link    {color: white; }
        a.th:visited {color: white; }
    </style>
    <title>Cookbook Node Map: <xsl:value-of select="$thisExample"/></title>
  </head>

  <body>
    <h1>Cookbook Node Map: <xsl:value-of select="$thisExample"/></h1>

    <p>
      <b><xsl:value-of select="$oaVersion"/></b>
    </p>
    <p>
        <a href="../{$thisExample}.xml"><xsl:value-of select="$thisExample"/>.xml</a>
    </p>
    <p>
        <a href="../{$thisExample}.html">Generated documentation for <xsl:value-of select="$thisExample"/></a>
    </p>
    <p>
        <a href="../cookbook2beans.html#{$thisExample}">Cookbook to Beans index for <xsl:value-of select="$thisExample"/>.</a>
    </p>

    <img src="{$thisExample}.{$imageFileExtension}" usemap="#Map{translate($thisExample, '/', '_')}" alt=""/>
    <xsl:copy-of select="document(concat('../../build/images/',$thisExample,'.map'))"/>

    <pre><xsl:value-of select="beans:beans/beans:description"/></pre>
  </body>
</html>
</xsl:template>
</xsl:stylesheet>

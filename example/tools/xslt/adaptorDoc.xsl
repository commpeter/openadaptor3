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

    $HeadURL$

    @author Andrew Shire

    Autogenerates detailed documentation for OA3 XML files
    (interprets Spring config with knowledge of key OA3 semantics).

    Used by "beans.xsl" which generates documentation for an entire config example.
  -->
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns="http://www.w3.org/TR/xhtml1/strict"
                xmlns:beans="http://www.springframework.org/schema/beans">
    <!--
        Overall document template.
      -->
    <xsl:template match="/beans:beans">
        <xsl:param name="baseRelativeDotDot" select="'./'" />
        <!-- Get description for whole of this adaptor -->
        <xsl:if test="beans:description">
          <pre><xsl:apply-templates select="beans:description"/></pre>
        </xsl:if>
        <!-- Generate clickable table of contents for top-level beans -->
        <table border="1" bgcolor="#CCCCCC">
            <colgroup>
                <col bgcolor="#CCCCCC"/>
                <col bgcolor="#FFFFFF"/>
                <col bgcolor="#CCCCCC"/>
                <col bgcolor="#FFFFFF"/>
            </colgroup>

            <tr bgcolor="#000099">
                <th><font color="white">Beans index</font></th>
                <th><font color="white">Process routing</font></th>
                <th><font color="white">Discard routing</font></th>
                <th><font color="white">Exception routing</font></th>
            </tr>

            <!--  Loop through available routing information: -->
            <xsl:for-each select="beans:bean[(@id='Adaptor') or (@class='org.openadaptor.core.adaptor.Adaptor')]/beans:property[@name='messageProcessor']">
            
                <!-- Router or Pipeline directly-embedded in Adaptor: -->
                <xsl:for-each select="beans:bean[(@class='org.openadaptor.core.router.Router') or (@class='org.openadaptor.core.router.Pipeline')]">
                    <xsl:call-template name="routerRouting" />
                </xsl:for-each>
                    
                <!-- Router or Pipeline referred-to by Adaptor: -->
                <xsl:for-each select="@ref">
                    <xsl:variable name="nodeName" select="." />
                    <xsl:for-each select="/beans:beans/beans:bean[@id=$nodeName]">

                        <!-- Router or Pipeline referred-to by Adaptor: -->
                        <xsl:if test="(@class='org.openadaptor.core.router.Router') or (@class='org.openadaptor.core.router.Pipeline')">
                            <xsl:call-template name="routerRouting" />
                        </xsl:if>
                                            
                    </xsl:for-each>
                </xsl:for-each>
            </xsl:for-each>
        </table>
        
        <!-- Generate documentation for each bean -->
        <xsl:for-each select="beans:bean">
            <p>
                <xsl:apply-templates select=".">
                    <xsl:with-param name="baseRelativeDotDot" select="$baseRelativeDotDot" />
                </xsl:apply-templates>
            </p>
        </xsl:for-each>
    </xsl:template>


    <xsl:template name="routerRouting">
        <xsl:variable name="routerNode" select="." />
        <xsl:for-each select="/beans:beans/beans:bean">
            <xsl:variable name="beanId" select="@id" />
            <tr>
                <td>
                    <xsl:choose>
                        <!-- Named bean -->
                        <xsl:when test="@id">
                            <a href="#{@id}"><xsl:value-of select="@id"/></a>
                        </xsl:when>
                        <!-- Anonymous bean -->
                        <xsl:otherwise>
                            <xsl:value-of select="name()"/>
                        </xsl:otherwise>
                    </xsl:choose>
                </td>
                <td>
                    <!-- Router process routing: -->
                    <xsl:apply-templates select="$routerNode/beans:property[@name='processMap']/beans:map/beans:entry[@key-ref=$beanId]/@value-ref" />

                    <!-- Pipeline process routing: -->
                    <xsl:for-each select="$routerNode/beans:property[@name='processors']/beans:list/beans:ref">
                        <!-- Each entry in pipeline list routes to next entry in pipeline list: -->
                        <xsl:if test="(@bean=$beanId) and (position()!=last())">
                            <xsl:variable name="destPos" select="position()+1"/>
                            <xsl:apply-templates select="../beans:ref[position()=$destPos]"/>
                        </xsl:if>
                    </xsl:for-each>
                </td>
                <td>
                    <!-- Router discard routing: -->
                    <xsl:apply-templates select="$routerNode/beans:property[@name='discardMap']/beans:map/beans:entry[@key-ref=$beanId]/@value-ref" />

                    <!-- Pipeline discard routing: -->
                    <!-- No support for discard routing in pipelines construct. -->
                </td>
                <td>
                    <!-- Router exception routing: -->
                    <xsl:apply-templates select="$routerNode/beans:property[@name='exceptionMap']/beans:map/beans:entry[@key-ref=$beanId]/@value-ref" />
                    
                    <!-- Pipeline exception routing: -->
                    <xsl:apply-templates select="$routerNode/beans:property[@name='exceptionProcessor']"/>
                </td>
            </tr>
         </xsl:for-each>
    </xsl:template>



    <!--
        Templates for bean.
      -->
    <xsl:template match="beans:bean">
        <xsl:param name="baseRelativeDotDot" select="'./'" />
        <table border="1" bgcolor="#CCCCCC">
            <colgroup>
                <col bgcolor="#CCCCCC"/>
                <col bgcolor="#FFFFFF"/>
                <col bgcolor="#CCCCCC"/>
                <col bgcolor="#FFFFFF"/>
            </colgroup>

            <tr bgcolor="#000099"><th colspan="3"><font color="white">
                <xsl:choose>
                    <!-- Named bean -->
                    <xsl:when test="@id">
                        <a name="{@id}"></a>
                        <a class="th" href="{$baseRelativeDotDot}beans2config.html#{@class}">
                            <xsl:value-of select="@id"/>
                        </a>
                    </xsl:when>
                    <!-- Anonymous bean -->
                    <xsl:otherwise>
                        <a class="th" href="{$baseRelativeDotDot}beans2config.html#{@class}">
                            <xsl:value-of select="name()"/>
                        </a>
                    </xsl:otherwise>
                </xsl:choose>
                <!-- Beans always have a class, unless they refer to a factory bean: -->
                <xsl:if test="@class">
                    <xsl:text> : </xsl:text>
                    <a class="th" href="{$baseRelativeDotDot}../javadocs/{translate(@class,'.','/')}.html">
                      <xsl:value-of select="@class"/>
                    </a>
                </xsl:if>
                <xsl:if test="@factory-bean">
                    <xsl:text> : factory-bean=</xsl:text>
                    <a class="th" href="{@factory-bean}">
                      <xsl:value-of select="@factory-bean"/>
                    </a>
                </xsl:if>
            </font></th></tr>

            <xsl:if test="beans:description">
                <tr bgcolor="#CCCCCC"><td colspan="3">
                    <xsl:apply-templates select="beans:description"/>
                </td></tr>
            </xsl:if>
            <xsl:if test="beans:property">
                <tr><td colspan="3">
                        <xsl:apply-templates select="beans:property"/>
                </td></tr>
            </xsl:if>
        </table>
    </xsl:template>

    <!--
        Templates for the properties.
      -->
    <xsl:template match="beans:property[@value]">
        <tr bgcolor="#FFFFFF">
            <td bgcolor="#CCCCCC"><xsl:value-of select="@name"/></td>
            <td><xsl:value-of select="@value"/></td>
            <td><xsl:apply-templates select="beans:description"/></td>
        </tr>
    </xsl:template>

    <xsl:template match="beans:property[@ref]">
        <tr bgcolor="#FFFFFF">
            <td bgcolor="#CCCCCC"><xsl:value-of select="@name"/></td>
            <td><a href="#{@ref}"><xsl:value-of select="@ref"/></a></td>
            <td><xsl:apply-templates select="beans:description"/></td>
        </tr>
    </xsl:template>

    <xsl:template match="beans:property[not(@value) and not(@ref)]">
        <tr bgcolor="#FFFFFF">
            <td bgcolor="#CCCCCC"><xsl:value-of select="@name"/></td>
            <td><xsl:apply-templates select="*"/></td>
        </tr>
    </xsl:template>

    <!--
        Templates for the structured data types.
        Note these do not include name of object (to aid reuse in docs).
      -->
    <xsl:template match="beans:list">
        <table border="1" bgcolor="#FFFFFF">
            <xsl:for-each select="*">
                <tr>
                    <td><xsl:value-of select="position()"/></td>
                    <td><xsl:apply-templates select="."/></td>
                </tr>
            </xsl:for-each>
        </table>
    </xsl:template>

    <xsl:template match="beans:map">
        <table border="1" bgcolor="#FFFFFF">
            <xsl:apply-templates select="beans:entry"/>
        </table>
    </xsl:template>

    <xsl:template match="beans:props">
        <table border="1" bgcolor="#FFFFFF">
            <xsl:apply-templates select="beans:prop"/>
        </table>
    </xsl:template>

    <!--
        Templates for the compound data types used by structured data types.
      -->
    <xsl:template match="beans:entry">
        <tr>
            <td><xsl:value-of select="position()"/></td>
            <td><xsl:apply-templates select="beans:key|@key|@key-ref"/></td>
            <td><xsl:apply-templates select="beans:ref|@ref"/></td>
            <td><xsl:apply-templates select="beans:value|@value|@value-ref"/></td>
            <td><xsl:value-of select="descendant-or-self::comment()"/></td>
        </tr>
    </xsl:template>

    <xsl:template match="beans:prop">
        <tr>
            <td><xsl:value-of select="position()"/></td>
            <td><xsl:apply-templates select="@key"/></td>
            <td><xsl:apply-templates select="descendant::node()"/></td>
            <td><xsl:value-of select="descendant-or-self::comment()"/></td>
        </tr>
    </xsl:template>

    <!--
        Templates for the basic data types.
        Note these are deliberately minimalist (to aid reuse in docs).
      -->
    <xsl:template match="beans:key">
        <xsl:value-of select="beans:value"/>
    </xsl:template>

    <xsl:template match="beans:value">
        <xsl:value-of select=".|@*"/>
    </xsl:template>

    <xsl:template match="beans:ref">
        <xsl:if test="@bean">
            <a href="#{@bean}"><xsl:value-of select="@bean"/></a>
        </xsl:if>
        <xsl:if test="@local">
            <a href="#{@bean}"><xsl:value-of select="@local"/></a>
        </xsl:if>
        <xsl:value-of select="."/>
    </xsl:template>

    <xsl:template match="@ref|@key-ref|@value-ref">
        <a href="#{.}"><xsl:value-of select="."/></a>
    </xsl:template>

</xsl:stylesheet>

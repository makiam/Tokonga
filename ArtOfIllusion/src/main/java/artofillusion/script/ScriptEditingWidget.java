/* Copyright (C) 2023 by Lucas Stanek
 * Changes copyright 2023 by Maksim Khramov
 *
 *This program is free software; you can redistribute it and/or
 *modify it under the terms of the GNU General Public License
 *as published by the Free Software Foundation; either version 2
 *of the License, or (at your option) any later version.
 *
 *This program is distributed in the hope that it will be useful,
 *but WITHOUT ANY WARRANTY; without even the implied warranty of
 *MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *GNU General Public License for more details.
 *
 *You should have received a copy of the GNU General Public License
 *along with this program; if not, write to the Free Software
 *Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package artofillusion.script;

import buoy.widget.*;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.fife.ui.rsyntaxtextarea.*;
import org.fife.ui.rtextarea.Gutter;

/**
 * Shared code for setting up an editing widget for aoi scripts.
 *
 * This is a widget that can display and edit script text, in either
 * Groovy or Beanshell. It exists primarily to provide a unified
 * styling across the various script editors. It has a fixed preferred
 * size. Saving, loading, and evaluation of the scripts should be
 * handled by the calling code.
 */
@Slf4j
public class ScriptEditingWidget extends BScrollPane
{
  public ScriptEditingWidget(String script)
  {
    super(new RSTextArea(script, 25, 100), SCROLLBAR_AS_NEEDED, SCROLLBAR_ALWAYS);
    RSyntaxTextArea rsta = getContent().getComponent();
    setRowHeader(new AWTWidget(new Gutter(rsta)));

    try{
      Theme theme = Theme.load(ScriptEditingWidget.class.getResourceAsStream("/scriptEditorTheme.xml"));
      theme.apply(rsta);
    } catch (IOException ex)
    {
      //shouldn't happen unless we are pointing at a non-existant file
      log.atError().setCause(ex).log("Unable to load Editor theme: {}", ex.getMessage());
    }

    rsta.setAnimateBracketMatching(false);
    rsta.setTabSize(2);
  }

  //TODO: migrate to constant, rather than string
  public void setLanguage(String lang)
  {
    getContent().getComponent().setSyntaxEditingStyle(lang.equalsIgnoreCase("groovy")
                                   ? SyntaxConstants.SYNTAX_STYLE_GROOVY
                                   : SyntaxConstants.SYNTAX_STYLE_JAVA);
  }

  @Override
  public RSTextArea getContent()
  {
    return (RSTextArea) super.getContent();
  }

  public static class RSTextArea extends BTextArea
  {
    public RSTextArea(String contents, int rows, int columns)
    {
      super(contents, rows, columns);
    }

    @Override
    protected RSyntaxTextArea createComponent()
    {
      return new RSyntaxTextArea();
    }

    @Override
    public RSyntaxTextArea getComponent()
    {
      return (RSyntaxTextArea) component;
    }

    @Override
    public void setBackground(java.awt.Color color)
    {
      getComponent().setBackground(color);
    }
  }
}

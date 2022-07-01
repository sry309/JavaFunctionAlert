package utility;

import alert.JdkFuncAlert;
import com.intellij.codeInsight.highlighting.HighlightManager;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.compiled.ClsFileImpl;
import com.intellij.ui.JBColor;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class HighLightUtility {
    public static List<TextRange> alertJdkTextRangeList = new ArrayList<TextRange>();
    public static List<TextRange> alertSinkTextRangeList = new ArrayList<TextRange>();

    public static void addHighLight(Project project, PsiFile psiFile){
        alertJdkTextRangeList.clear();
        alertSinkTextRangeList.clear();

        final HighlightManager highlightManager = HighlightManager.getInstance(project);

        // jdk灰色
        TextAttributesKey jdkTextAttributesKey = TextAttributesKey.createTextAttributesKey("CONSOLE_NORMAL_OUTPUT");
        jdkTextAttributesKey.getDefaultAttributes().setFontType(Font.BOLD);

        // sink红色
        TextAttributesKey sinkTextAttributesKey = TextAttributesKey.createTextAttributesKey("CONSOLE_USER_INPUT");
        sinkTextAttributesKey.getDefaultAttributes().setForegroundColor(JBColor.RED);

        // 获取editor
        Editor editor = FileEditorManager.getInstance(project).getSelectedTextEditor();
        if (editor==null){
            return;
        }

        try {
            //从ClsFileImpl文件获取到PsiJavaFileImpl
            if(psiFile instanceof ClsFileImpl)
                ((ClsFileImpl) psiFile).getDecompiledPsiFile().accept(new JdkFuncAlert());
            else
                psiFile.accept(new JdkFuncAlert());

            //给jdk高亮
            for (TextRange textRange:alertJdkTextRangeList
            ) {
                highlightManager.addRangeHighlight(editor,textRange.getStartOffset(),textRange.getEndOffset(),jdkTextAttributesKey,true,null);
            }

            //给sink高亮
            for (TextRange textRange:alertSinkTextRangeList
            ) {
                highlightManager.addRangeHighlight(editor,textRange.getStartOffset(),textRange.getEndOffset(),sinkTextAttributesKey,true,null);
            }
        }catch (Exception e){
            System.out.println(e.getMessage());
        }
    }
}

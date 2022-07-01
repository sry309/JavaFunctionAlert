package main;

import com.intellij.lang.java.JavaLanguage;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ex.ToolWindowManagerListener;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.util.messages.MessageBus;
import org.jetbrains.annotations.NotNull;
import utility.HighLightUtility;

import static alert.JdkFuncAlert.loadSinkConfig;

public class FileListener implements ToolWindowManagerListener {

    public FileListener(Project project) {
        // 加载sink、jdk配置
        loadSinkConfig();

        //监控文件打开变化
        MessageBus messageBus = project.getMessageBus();
        messageBus.connect().subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, new  FileEditorManagerListener() {
            @Override
            public void fileOpened(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
                FileViewProvider fileViewProvider = PsiManager.getInstance(project).findViewProvider(file);
                if(fileViewProvider!=null){
                    PsiFile psiFile = fileViewProvider.getPsi(JavaLanguage.INSTANCE);
                    HighLightUtility.addHighLight(project,psiFile);
                }

            }
        });

        //监控文件修改变化
        messageBus.connect().subscribe(com.intellij.AppTopics.FILE_DOCUMENT_SYNC,new FileDocumentManagerListener(){
            @Override
            public void beforeDocumentSaving(@NotNull Document document) {
                VirtualFile virtualFile = FileDocumentManager.getInstance().getFile(document);
                if (virtualFile==null){
                    return;
                }
                FileViewProvider fileViewProvider = PsiManager.getInstance(project).findViewProvider(virtualFile);
                if(fileViewProvider!=null){
                    PsiFile psiFile = fileViewProvider.getPsi(JavaLanguage.INSTANCE);
                    HighLightUtility.addHighLight(project,psiFile);
                }
            }
        });

    }
}
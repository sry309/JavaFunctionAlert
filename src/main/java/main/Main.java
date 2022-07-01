package main;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

import static alert.JdkFuncAlert.loadSinkConfig;


public class Main extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
        // 手动更新sink列表
        loadSinkConfig();
    }
}

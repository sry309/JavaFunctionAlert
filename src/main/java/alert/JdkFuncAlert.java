package alert;


import com.intellij.psi.*;
import com.intellij.psi.impl.source.tree.java.PsiMethodCallExpressionImpl;
import com.intellij.psi.impl.source.tree.java.PsiNewExpressionImpl;
import org.jetbrains.annotations.NotNull;
import utility.FileUtility;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static utility.FileUtility.readYamlFile;
import static utility.HighLightUtility.alertJdkTextRangeList;
import static utility.HighLightUtility.alertSinkTextRangeList;


public class JdkFuncAlert extends PsiRecursiveElementVisitor {

    public static List<String> jdkClazzList = new ArrayList<String>();

    public static List<String> sinkList = new ArrayList<String>();

    public static List<String> getAllSinkList(Map<String, Object> sinkTypeListMap){
        List<String> resultList = new ArrayList<String>();
        for (String sinkType : sinkTypeListMap.keySet()) {
            // 加载第一层
            if(sinkTypeListMap.get(sinkType) instanceof List){
                resultList.addAll((Collection<? extends String>) sinkTypeListMap.get(sinkType));
            }else {
                //加载第二层
                Object subSinkTypeListObj = sinkTypeListMap.get(sinkType);
                if(subSinkTypeListObj instanceof Map){
                    Map<String, Object> subSinkTypeListMap = (Map<String, Object>) subSinkTypeListObj;
                    for (String subSinkType : subSinkTypeListMap.keySet()) {
                        resultList.addAll((Collection<? extends String>) subSinkTypeListMap.get(subSinkType));
                    }
                }
            }
        }
        return resultList;
    }

    public static void loadSinkConfig() {
        String usrHome = System.getProperty("user.home");
        Path sinkPath = Paths.get(usrHome,"sink.yaml");
        String sinkPathStr = sinkPath.toAbsolutePath().toString();

        if(new File(sinkPathStr).exists()){
            // 如果~/sink.yaml存在就从磁盘加载
            sinkList.addAll(getAllSinkList(FileUtility.readYamlFile(sinkPathStr)));
        }else {
            // 如果磁盘不存在，则就从jar包里面的yaml加载
            String originSinkPathStr = Objects.requireNonNull(JdkFuncAlert.class.getClassLoader().getResource("sink.yaml")).toString();
            List<String> sinkFromFileList = getAllSinkList(readYamlFile(originSinkPathStr));
            sinkList.addAll(sinkFromFileList);
            // 并且保存一份到磁盘
            FileUtility.saveFile(sinkPathStr,FileUtility.readFile(originSinkPathStr));
        }

        // jdk的直接从jar包中加载
        List<String> jdkList = getAllSinkList(readYamlFile(Objects.requireNonNull(JdkFuncAlert.class.getClassLoader().getResource("jdk.yaml")).toString()));
        jdkClazzList.addAll(jdkList);
    }

    @Override
    public void visitElement(@NotNull PsiElement element) {
        if(element instanceof PsiMethodCallExpressionImpl)
        {
            String clazz = "";
            String method = "";
            PsiReferenceExpression methodExpression = ((PsiMethodCallExpressionImpl) element).getMethodExpression();
            PsiExpression psiExpression = methodExpression.getQualifierExpression();
            PsiType psiType = null;
            if(psiExpression!=null){
                psiType = psiExpression.getType();
            }

            if(psiType==null){
                // Class.forname 这种静态方法
                PsiElement QualifierPsiElement = methodExpression.getQualifier();
                if ((QualifierPsiElement != null) && (QualifierPsiElement.getReference()!=null)) {
                    clazz = QualifierPsiElement.getReference().getCanonicalText();
                }
            }else {
                //PsiReferenceExpression  System.out.println 中的Syste.out获取返回类型java.io.PrintStream, 需要getype动态计算出为PrintStream
                if(methodExpression.getQualifierExpression()!=null){
                    if(methodExpression.getQualifierExpression().getType()!=null){
                        clazz = methodExpression.getQualifierExpression().getType().getCanonicalText();
                    }
                }
            }

            // 解析方法
            if(methodExpression.getReferenceNameElement()!=null){
                method = methodExpression.getReferenceNameElement().getText();
            }

            String canonicalCalledMethod = clazz + "." + method;

            if(checkIsSink(canonicalCalledMethod)){
                if(methodExpression.getReferenceNameElement()!=null){
                    // sink，添加高亮
                    alertSinkTextRangeList.add(methodExpression.getReferenceNameElement().getTextRange());
                }
            }else if(checkIsJdk(clazz)){
                if(methodExpression.getReferenceNameElement()!=null){
                    // 如果是jdk，则把高亮的文本加入进去
                    alertJdkTextRangeList.add(methodExpression.getReferenceNameElement().getTextRange());
                }
            }

        }else if(element instanceof PsiConstructorCall){
            String clazz = "";
            //获取new String("") 中的java.lang.String，通过getType()获取的
            if(((PsiNewExpressionImpl) element).getType()!=null){
                clazz = ((PsiNewExpressionImpl) element).getType().getCanonicalText();
            }

            if(checkIsJdk(clazz)){
                //获取new String("") 中的String的位置
                if(((PsiNewExpressionImpl) element).getClassReference()!=null)
                    alertJdkTextRangeList.add(((PsiNewExpressionImpl) element).getClassReference().getTextRange());
            }
        }

        super.visitElement(element);
    }

    public boolean checkIsJdk(String clazz){
        for (String jdkClazz:jdkClazzList
             ) {
            if (clazz.startsWith(jdkClazz)){
                return true;
            }
        }
        return false;
    }

    public boolean checkIsSink(String canonicalCalledMethod){
        return sinkList.contains(canonicalCalledMethod);
    }
}

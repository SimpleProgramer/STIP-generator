package com.stip.mybatis.generator.plugin;


import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.mybatis.generator.api.GeneratedJavaFile;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.JavaFormatter;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.ShellCallback;
import org.mybatis.generator.api.dom.java.CompilationUnit;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.JavaVisibility;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.exception.ShellException;
import org.mybatis.generator.internal.DefaultShellCallback;
import org.mybatis.generator.internal.util.StringUtility;

/**
 * 生成Model 的相关子类
 * 
 * @author cja
 *
 */
public class ModelAndExampleSubClassPlugin extends PluginAdapter {

    private ShellCallback shellCallback = null;

    /**
     * Model基类文件包名
     */
    private String baseModelPackage;

    /**
     * Model类的前缀名称
     */
    private String baseModelNamePrefix;

    public ModelAndExampleSubClassPlugin() {
        shellCallback = new DefaultShellCallback(false);
    }

    public boolean validate(List<String> warnings) {

        baseModelPackage = properties.getProperty("baseModelPackage");
        if (!StringUtility.stringHasValue(baseModelPackage)) {
            baseModelPackage = ModelAndExampleBaseClassPlugin.DEFAULT_BASE_MODEL_PACKAGE;
        }

        baseModelNamePrefix = properties.getProperty("baseModelNamePrefix");
        if (!StringUtility.stringHasValue(baseModelNamePrefix)) {
            baseModelNamePrefix = ModelAndExampleBaseClassPlugin.DEFAULT_BASE_MODEL_NAME_PREFIX;
        }

        return true;
    }

    @Override
    public List<GeneratedJavaFile> contextGenerateAdditionalJavaFiles(IntrospectedTable introspectedTable) {
        System.out.println("===============开始：生成Model子类文件================");

        JavaFormatter javaFormatter = context.getJavaFormatter();

        List<GeneratedJavaFile> subClassJavaFiles = new ArrayList<GeneratedJavaFile>();
        for (GeneratedJavaFile javaFile : introspectedTable.getGeneratedJavaFiles()) {

            CompilationUnit unit = javaFile.getCompilationUnit();
            FullyQualifiedJavaType baseModelJavaType = unit.getType();

            TopLevelClass subModelClass = new TopLevelClass(getSubModelType(baseModelJavaType));

            subModelClass.setVisibility(JavaVisibility.PUBLIC);
            subModelClass.addImportedType(baseModelJavaType);
            subModelClass.setSuperClass(baseModelJavaType);

            if (!baseModelJavaType.getFullyQualifiedName().endsWith("Example")) {// 对Example类不能添加序列化版本字段
                Field field = new Field("serialVersionUID", new FullyQualifiedJavaType("long"));
                field.setStatic(true);
                field.setFinal(true);
                field.setVisibility(JavaVisibility.PRIVATE);
                field.setInitializationString("1L");
                subModelClass.addField(field);
            }

            String targetProject = javaFile.getTargetProject();
            FullyQualifiedJavaType subModelJavaType = subModelClass.getType();
            String subModelPackageName = subModelJavaType.getPackageName();

            try {
                GeneratedJavaFile subCLassJavafile = new GeneratedJavaFile(subModelClass, targetProject, javaFormatter);

                File subModelDir = shellCallback.getDirectory(targetProject, subModelPackageName);

                File subModelFile = new File(subModelDir, subCLassJavafile.getFileName());

                // 文件不存在
                if (!subModelFile.exists()) {

                    subClassJavaFiles.add(subCLassJavafile);
                }
            } catch (ShellException e) {
                e.printStackTrace();
            }

        }

        System.out.println("===============结束：生成Model子类文件================");

        return subClassJavaFiles;
    }

    private String getSubModelType(FullyQualifiedJavaType fullyQualifiedJavaType) {
        String type = fullyQualifiedJavaType.getFullyQualifiedName();
        String defaultPrefix = baseModelPackage + "." + baseModelNamePrefix;
        String newType = type.replace(defaultPrefix, "");
        return newType;
    }
}

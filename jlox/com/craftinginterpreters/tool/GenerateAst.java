package com.craftinginterpreters.tool;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

public class GenerateAst {
	public static void main(String[] args) throws IOException {
		if (args.length != 1) {
			System.err.println("Usage: generate_ast <output directory>");
			System.exit(64);
		}
		String outputDir = args[0];
		defineAst(outputDir, "Expr", Arrays.asList(
			"Unary    : Token operator, Expr right",
			"Binary   : Expr left, Token operator, Expr right",
			"Grouping : Expr expression",
			"Literal  : Object value"
		));
	}

	private static void defineAst(
		String outputDir, String baseName, List<String> types) 
		throws IOException {
		
		String path = outputDir + "/" + baseName + ".java";
		PrintWriter writter = new PrintWriter(path, "UTF-8");

		writter.println("package com.craftinginterpreters.lox;");
		writter.println();
		writter.println("import java.util.List;");
		writter.println();
		writter.println("abstract class " + baseName + " {");

		defineVisitor(writter, baseName, types);

		// AST classes
		for (String type : types) {
			String className = type.split(":")[0].trim();
			String fields = type.split(":")[1].trim();
			defineType(writter, baseName, className, fields);
		}

		// Base accept function
		writter.println("\tabstract <R> R accept(Visitor<R> visitor);");

		writter.println("}");
		writter.close();
	}

	private static void defineType(
		PrintWriter writer, String baseName, 
		String className, String fieldList) {
		
		writer.println("\tstatic class " + className + " extends " + baseName + " {");

		// Construtor
		writer.println("\t\t" + className + "(" + fieldList + ") {");

		// Colocar valores nos atributos
		String[] fields = fieldList.split(", ");
		for (String field : fields) {
			String name = field.split(" ")[1];
			writer.println("\t\t\tthis." + name + " = " + name + ";");
		}

		writer.println("\t\t}");
		// Fim do construtor

		writer.println();

		writer.println("\t\t@Override");
		writer.println("\t\t<R> R accept(Visitor<R> visitor) {");
		writer.println("\t\t\treturn visitor.visit" + className + baseName + "(this);");
		writer.println("\t\t}\n");

		// Atributos
		for (String field : fields) {
			writer.println("\t\tfinal " + field + ";");
		}

		writer.println("\t}\n");
	}

	private static void defineVisitor(
		PrintWriter writer, String baseName, List<String> types) {
		
		writer.println("\tinterface Visitor<R> {");

		for (String type : types) {
			String typeName = type.split(":")[0].trim();
			writer.println("\t\tR visit" + typeName + baseName + 
				"(" + typeName + " " + baseName.toLowerCase() + ");");
		}

		writer.println("\t}\n");
	}
}

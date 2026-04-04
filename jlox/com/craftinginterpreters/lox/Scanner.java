package com.craftinginterpreters.lox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.craftinginterpreters.lox.TokenType.*;

class Scanner {
	private final String source;
	private final List<Token> tokens = new ArrayList<>();
	private static final Map<String, TokenType> keywords;
	private int start = 0;
	private int current = 0;
	private int line = 1;

	Scanner(String source) {
		this.source = source;
	}

	static {
		keywords = new HashMap<>();
		keywords.put("and",    AND);
		keywords.put("or",     OR);
		keywords.put("if",     IF);
		keywords.put("else",   ELSE);
		keywords.put("for",    FOR);
		keywords.put("while",  WHILE);
		keywords.put("true",   TRUE);
		keywords.put("false",  FALSE);
		keywords.put("nil",    NIL);
		keywords.put("class",  CLASS);
		keywords.put("super",  SUPER);
		keywords.put("this",   THIS);
		keywords.put("fun",    FUN);
		keywords.put("var",    VAR);
		keywords.put("print",  PRINT);
		keywords.put("return", RETURN);
	}

	private boolean isAtEnd() {
		return current >= source.length();
	}

	List<Token> scanTokens() {
		while (!isAtEnd()) {
			// Começo do próximo lexema
			start = current;
			scanToken();
		}

		tokens.add(new Token(EOF, "", null, line));
		return tokens;
	}

	private void scanToken() {
		char c = advance();
		switch (c) {
			case '(': addToken(LEFT_PAREN); break;
			case ')': addToken(RIGHT_PAREN); break;
			case '{': addToken(LEFT_BRACE); break;
			case '}': addToken(RIGHT_BRACE); break;
			case ',': addToken(COMMA); break;
			case '.': addToken(DOT); break;
			case '-': addToken(MINUS); break;
			case '+': addToken(PLUS); break;
			case ';': addToken(SEMICOLON); break;
			case '*': addToken(STAR); break;
			case '!':
				addToken(match('=') ? BANG_EQUAL : BANG);
				break;
			case '=':
				addToken(match('=') ? EQUAL_EQUAL : EQUAL);
				break;
			case '>':
				addToken(match('=') ? GREATER_EQUAL : GREATER);
				break;
			case '<':
				addToken(match('=') ? LESS_EQUAL : LESS);
				break;
			case '/':
				if (match('/')) {
					// É um comentário inline
					while (peek() != '\n' && !isAtEnd()) advance();
					// !isAtEnd() na esquerda para curto-circuito?
				} else {
					addToken(SLASH);
				}
				break;

			case ' ':
			case '\r':
			case '\t':
				// Ignora espaços em branco
				break;
			case '\n':
				line++;
				break;
			
			case '"': string(); break;
		
			default:
				if (isDigit(c)) {
					number();
				} else if (isAlpha(c)) {
					identifier();
				} else {
					Lox.error(line, "Unexpected character.");
				}
				break;
		}
	}

	// Consome um caractere e retorna ele
	private char advance() {
		return source.charAt(current++);
	}

	// Só consome um caractere se for o esperado
	private boolean match(char expected) {
		if (isAtEnd()) return false;
		if (source.charAt(current) != expected) return false;

		current++;
		return true;
	}

	// Não consome um caractere, mas retorna ele - lookahead de 1
	private char peek() {
		if (isAtEnd()) return '\0';
		return source.charAt(current);
	}

	// Lookahead de 2
	private char peekNext() {
		if (current + 1 >= source.length()) return '\0';
		return source.charAt(current + 1);
	}

	private void addToken(TokenType type) {
		addToken(type, null);
	}

	private void addToken(TokenType type, Object literal) {
		String text = source.substring(start, current);
		tokens.add(new Token(type, text, literal, line));
	}

	private void string() {
		while (peek() != '"' && !isAtEnd()) {
			if (peek() == '\n') line++;
			advance();
		}

		if (isAtEnd()) {
			Lox.error(line, "Unterminated string.");
			return;
		}

		// Consome as " finais
		advance();

		// Corta as "" da string
		String value = source.substring(start+1, current-1);
		addToken(STRING, value);
	}

	private void number() {
		// Dígitos antes do '.'
		while (isDigit(peek())) advance();

		// Deveria lançar um erro se terminar número com '.'
		if (peek() == '.' && isDigit(peekNext())) {
			// Consome o '.'
			advance();

			// Dígitos após o '.'
			while (isDigit(peek())) advance();
		}

		addToken(NUMBER, Double.parseDouble(source.substring(start, current)));
	}

	private void identifier() {
		while (isAlphaNumeric(peek())) advance();

		String text = source.substring(start, current);
		TokenType type = keywords.get(text);
		if (type == null) type = IDENTIFIER;
		addToken(type);
	}

	private boolean isDigit(char c) {
		return c >= '0' && c <= '9';
	}

	private boolean isAlpha(char c) {
		return (c >= 'a' && c <= 'z') ||
			   (c >= 'A' && c <= 'Z') ||
			    c == '_';		
	}

	private boolean isAlphaNumeric(char c) {
		return isAlpha(c) || isDigit(c);
	}

}

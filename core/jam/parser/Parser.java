package core.jam.parser;

import core.jam.expressions.*;
import core.jam.typing.*;
import java.util.ArrayList;
import java.util.StringTokenizer;

/*
 * @author Mohamed Adam Chaieb
 * 
 * This class is the parser for the Jam programming language.
 * */

public class Parser
{
  private ArrayList<String> tokens;
  private ArrayList<Expression> expressions;
  private ConstraintSet typeConstraints;
  private TypeContext typeContext;
  private ArrayList<Binding> variableContext;
  
  public Parser(String program)
  {
    this.tokens = new ArrayList<String>();
    this.expressions = new ArrayList<Expression>();
    this.typeConstraints = ConstraintSet.EMPTY;
    this.typeContext = TypeContext.EMPTY;
    this.variableContext = new ArrayList<Binding>();
    StringTokenizer tokenizer = new StringTokenizer(program,";",false);
    while(tokenizer.hasMoreTokens())
      this.tokens.add(tokenizer.nextToken());
  }
  
  public String toString()
  {
    String output = "[";
    for(int i = 0; i<this.tokens.size()-1;i++)
      output += this.tokens.get(i)+",";
    output += this.tokens.get(this.tokens.size()-1)+"]";
    return output;
  }
  
  public ArrayList<String> getTokens()
  {
    return this.tokens;
  }
  
  public ArrayList<Expression> getExpressions()
  {
    return this.expressions;
  }
  
  //creates the expression object from the
  public Expression makeExpression(String in)
  {
    String input = in.trim();
    StringBuilder builder = new StringBuilder(in);
    Expression output = Expression.EMPTY;
    //base cases
    if(Int.isInt(input))
      output = new Int(in);
    else if(Real.isReal(input))
      output = new Real(in);
    else if(core.jam.expressions.Boolean.isBoolean(input))
      output = new core.jam.expressions.Boolean(in);
    else if(input.startsWith(Keyword.FN.toString()))
    {
      Variable var = new Variable(input.substring(3,input.indexOf(Keyword.ARROW.toString())).trim());
      Expression e = this.makeExpression(input.substring(input.indexOf(Keyword.ARROW.toString())+2).trim());
      output = new Function(var, e);
      this.typeContext.augment(var, e.type);
    }
    else if(input.startsWith(Keyword.IF.toString()))
      output = new If(this.makeExpression(input.substring(3,input.indexOf(Keyword.THEN.toString())).trim()), 
                    this.makeExpression(input.substring(input.indexOf(Keyword.THEN.toString())+4, input.indexOf(Keyword.ELSE.toString())).trim()),
                    this.makeExpression(input.substring(input.indexOf(Keyword.ELSE.toString())+4).trim()));
    else if(input.startsWith(Keyword.REC.toString()))
      output = new Recursion(new Variable(input.substring(4,input.indexOf(Keyword.ARROW.toString())).trim()), 
                             this.makeExpression(input.substring(input.indexOf(Keyword.ARROW.toString())+2).trim()));
    else if(input.startsWith(Keyword.LET.toString()))
    {
      Variable var = new Variable(input.substring(4,input.indexOf(Keyword.EQUALS.toString())).trim());
      Expression e = this.makeExpression(input.substring(input.indexOf(Keyword.EQUALS.toString())+1, input.indexOf(Keyword.IN.toString())).trim());
      output = new Let(new Binding(var, e), this.makeExpression(input.substring(input.indexOf(Keyword.IN.toString())+3).trim()));
      this.typeContext.augment(var, e.type);
    }
    else if(Character.isDigit(in.charAt(0)))//if first character is a digit, but the expression is not a number
    {
      int index = BinaryOperator.index(input);
      BinaryOperator operator = BinaryOperator.getOperatorAt(index, input);
      output = new BinaryOperation(this.makeExpression(input.substring(0,index).trim()), 
                                   this.makeExpression(input.substring(index+operator.op.length()).trim()),
                                   operator);
    }
    else if(UnaryOperator.startsWithOperator(input))
      output = new UnaryOperation(this.makeExpression(input.substring(UnaryOperator.getOperator(input).op.length())),
                                  UnaryOperator.getOperator(input));
    else if(Variable.isAllowed(input))
      output = new Variable(input);
    this.typeConstraints = output.infer(this.typeContext);
    return output;
  }
}
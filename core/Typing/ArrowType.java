/*
 * @author Mohamed Adam Chaieb
 * 
 * This class represents the function type T1 -> T2
 * */

public class ArrowType extends Type
{
  public Type inputType;
  public Type outputType;
  
  public ArrowType(Type inputType, Type outputType)
  {
    this.inputType = inputType;
    this.outputType = outputType;
  }
  
  public Type substitute(Type sub, Type t)
  {
    return new ArrowType(this.inputType.substitute(sub,t), this.outputType.substitute(sub,t));
  }
  
  public boolean equals(Type other)
  {
    ArrowType t = (ArrowType)other;
    if(other instanceof ArrowType)
      return (this.inputType.equals(t.inputType) && this.outputType.equals(t.outputType));
    else return false;
  }
}
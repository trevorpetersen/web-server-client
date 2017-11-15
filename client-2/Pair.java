/* A class that pairs two objects together */
public class Pair<T1, T2>{
  private T1 first;
  private T2 second;

  public Pair(T1 first, T2 second){
    this.first = first;
    this.second = second;
  }

  public T1 getFirst(){
    return first;
  }

  public T2 getSecond(){
    return second;
  }


  public void setFirst(T1 newFirst){
    first = newFirst;
  }

  public void setSecond(T2 newSecond){
    second = newSecond;
  }

  public String toString(){
    return first.toString() + ": " + second.toString();
  }
}

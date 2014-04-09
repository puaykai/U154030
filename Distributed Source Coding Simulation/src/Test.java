import java.util.*;


public class Test {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		int[][] A = new int[][]{{1,2},{3,4}};
		
		int[][] B = new int[A.length][A[0].length];
		
		for(int i=0; i<A.length; i++){
			
			for(int j=0; j<A[0].length; j++){
				
				B[i][j] = A[i][j];
			}
		}
		
		B[0][0] = 0;
		
		System.out.println(A[0][0]);
	}

}

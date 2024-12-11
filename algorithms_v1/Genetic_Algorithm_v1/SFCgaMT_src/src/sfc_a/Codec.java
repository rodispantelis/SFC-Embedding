package sfc_a;

/*
The Edge Vector codec is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.
https://www.gnu.org/licenses/agpl-3.0.txt

Published by Pantelis Rodis, 2021
*/
	
	/** Edge Vector graph representation coder and doceder */

public class Codec {
	//encodes and decodes graph in the Edge Vector format
	//numbering starts from 0
	public Codec() {
	}
	
	/** Edge Vector coder */
	public int coder(int a, int b){
		//on input of two nodes, you get the place of their edge on the vector
		int x=0;
		if(a==b){
			System.out.println("Same node error: "+a+" | "+b);
			}else{
				if(a>b){
					int t=a;
					a=b;
					b=t;
				}
				x=((b-1)*(1+(b-1)))/2;
			}
		return x+a;

	}
	
	/** Edge Vector decoder */
	public int[] decoder(int q){
		//on input of one place of the vector that denotes one edge of the graph
		//you get the two nodes that define the edge
		//x<y
			
		int x=0;
		int y=0;

		y=(int) Math.sqrt((2*q));
		y++;
		x=q-(((y*y)-y)/2);
		
		if(x<0){
			y--;
			x=q-(((y*y)-y)/2);
		}		
		int result[]={x,y};
		return result;
		
	}
}

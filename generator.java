import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

class pattern
{
	int[][] label;
	int[][] ID;
	int start_vertex_ID;
	int end_vertex_ID;
	int patternID;
	int[][] sub_label;
	
	pattern(int[][] label,int[][] id,int start,int end, int patternID)
	{
		this.label=label;
		this.ID=id;
		this.start_vertex_ID=start;
		this.end_vertex_ID=end;
		this.patternID=patternID;
	}
	void generate_subMatrix()			// generate a sub_patttern and embed them into the negative graphs
	{
		int size=(int)(this.label.length*0.8);		// 0.8 gives how many vertices are kept in the sub_maxtrix
		this.sub_label=new int[size][size];
		for(int i=0;i<size;i++)
		{
			this.sub_label[i][i]=this.label[i][i];
			for(int j=0;j<size;j++)
			{
				this.sub_label[i][j]=this.label[i][j];
			}
		}
	}
}
public class generator {

	/**
	 * @param args
	 */
	static PrintWriter out;			       
	static FileWriter fw; 
	static int number_of_graphs=1000;
	static int size_of_graphs=100;
	static int size_of_pattern=5;
	static int number_of_pattern=10;		//number_of_patterns/2<0.3*number_of_graphs 
	static int errors_in_the_pattern=3;
	static String relative_address_prefix="scalability/number_of_graph/"+number_of_graphs;	//1000, 2500, 5000, 7500, 10000
//	static String relative_address_prefix="scalability/size_of_graph/"+size_of_graphs;		//100, 200, 300, 400, 500
//	static String relative_address_prefix="scalability/number_of_pattern/"+number_of_pattern;		//10, 20, 30, 40, 50
//	static String relative_address_prefix="scalability/size_of_pattern/"+size_of_pattern;		//5, 6, 7, 8, 9, 10
//	static String relative_address_prefix="effectiveness/default_setting_10";		//1,2,3,4,5,6,7,8,9,10
	static String pattern_address=relative_address_prefix+"/discriminative_patterns.txt";
	static String positive_graph_address=relative_address_prefix+"/positive_graphs.txt";
	static String negative_graph_address=relative_address_prefix+"/negative_graphs.txt";
	static int current_vertexID=1;
	static int current_vertex_label=1;
	static int current_edgeID=1;
	static int current_edge_label=1;
	static int current_patternID=0;
	static int num_padding_node=3;
	static ArrayList<pattern> pattern_list=new ArrayList<pattern>();				// store all the patterns
//	static int max=Integer.MAX_VALUE;   // use to define the range of the random number
	static int max=10000;   // this is for GAIA, GAIA use short to store the labels
	static int padding_vertexID;
	static int errors=1;
	static double pattern_fraction=0.01;
	static double error_percentage=1-0.2;
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		generate_discriminative_patterns();
		generate_subpatterns_in_negative_graphs();
		generate_positive_graphs();
		generate_negative_graphs();
	}
	static void generate_subpatterns_in_negative_graphs()
	{
		for(int i=0;i<pattern_list.size();i++)
		{
			pattern current_pattern=pattern_list.get(i);
			current_pattern.generate_subMatrix();
		}
	}
	static void generate_discriminative_patterns()
	{
		try
		{
			fw = new FileWriter(pattern_address,false); 
			out = new PrintWriter(fw); 
			out.println(number_of_pattern);
			for(int i=0;i<number_of_pattern;i++)
			{
				pattern p=generate_pattern();
				pattern_list.add(p);
				write_pattern(p);
			}
			close();
		}catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	static pattern generate_pattern()
	{
		pattern result;
		int[][] label=new int[size_of_pattern][size_of_pattern];
		int[][] ID=new int[size_of_pattern][size_of_pattern];
		int start=current_vertexID;
		int end=current_vertexID+size_of_pattern-1;
		for(int i=0;i<label.length;i++)			//set the matrix, the size of pattern is at least 2, otherwise there will be two edges between two vertices
		{
			label[i][i]=current_vertex_label;
			current_vertex_label++;
			ID[i][i]=i+1;
			if(i>0)
			{
				label[i][i-1]=current_edge_label;
				current_edge_label++;
				ID[i][i-1]=i;
			}
		}
		label[size_of_pattern-1][0]=current_edge_label;
		current_edge_label++;
		ID[size_of_pattern-1][0]=size_of_pattern;
		result=new pattern(label,ID,start,end,current_patternID);
		current_patternID++;
		return result;
	}
	static void write_pattern(pattern p)
	{
	/*	for(int i=0;i<size_of_pattern;i++)
		{
			for(int j=0;j<size_of_pattern;j++)
			{
				System.out.print(p.label[i][j]+" ");
			}
			System.out.println();
		}
		System.out.println();*/
		if(p.patternID>0)
		{
			out.println();
		}
		out.println("t # "+p.patternID+" "+size_of_pattern);
		for(int i=0;i<size_of_pattern;i++)
		{
			out.println("v "+p.ID[i][i]+" "+p.label[i][i]);
		}
		boolean first=true;
		for(int i=0;i<size_of_pattern;i++)
		{
			for(int j=0;j<i;j++)
			{
				if(p.label[i][j]!=0)
				{
					if(first==true)
					{
						first=false;
						out.print("e "+p.ID[j][j]+" "+p.ID[i][i]+" "+p.label[i][j]);
					}
					else
					{
						out.println();
						out.print("e "+p.ID[j][j]+" "+p.ID[i][i]+" "+p.label[i][j]);
					}
				}
			}
		}
	}
	static void generate_positive_graphs()		// connect the pattern to form a negative graph (0-60%),(61%-100% will be some random graphs)
	{
		try
		{
			fw = new FileWriter(positive_graph_address,false); 
			out = new PrintWriter(fw); 
			out.println(number_of_graphs);
			for(int i=0;i<number_of_graphs;i++)
			{
				padding_vertexID=size_of_pattern*number_of_pattern/2+1;
				out.println("t # "+i+" "+size_of_graphs);
				if(i<pattern_fraction*number_of_graphs)
				{
					//generate a positive graph based on the first half of the patterns
					int start_vertexID=1;
					for(int j=0;j<pattern_list.size()/2;j++)
					{
						pattern current_pattern=pattern_list.get(j);
						double random_number=Math.random();
						if(random_number<error_percentage)	// write the seed patterns
						{
							write_positive_graphs_pattern_vertex(current_pattern,false,start_vertexID);	// true means this is the first pattern
							start_vertexID=start_vertexID+size_of_pattern;
						}
						else		// write the variations of the seed pattern
						{
							write_positive_graphs_pattern_vertex(current_pattern,true,start_vertexID);
							start_vertexID=start_vertexID+size_of_pattern;
						}
					}
					write_positive_graphs_remaining_vertex(start_vertexID);
					int start_edgeID=1;
					int step=0;
					for(int j=0;j<pattern_list.size()/2;j++)
					{
						pattern current_pattern=pattern_list.get(j);
						step=write_positive_graphs_pattern_edge(current_pattern,start_edgeID);
						start_edgeID=start_edgeID+step;
					}
					write_positive_graphs_padding_and_remaining_edge(start_edgeID, true); // true means first half
				}
				else if(i<2*pattern_fraction*number_of_graphs)
				{
					//generate a positive graph based on the second half of the patterns 
					int start_vertexID=1;
					for(int j=pattern_list.size()/2;j<pattern_list.size();j++)
					{
						pattern current_pattern=pattern_list.get(j); 
						double random_number=Math.random();
						if(random_number<error_percentage)	// write the seed patterns
						{
							write_positive_graphs_pattern_vertex(current_pattern,false,start_vertexID);	// true means this is the first pattern
							start_vertexID=start_vertexID+size_of_pattern;
						}
						else		// write the variations of the seed pattern
						{
							write_positive_graphs_pattern_vertex(current_pattern,true,start_vertexID);
							start_vertexID=start_vertexID+size_of_pattern;
						}
					}
					write_positive_graphs_remaining_vertex(start_vertexID);
					int start_edgeID=1;
					for(int j=pattern_list.size()/2;j<pattern_list.size();j++)
					{
						pattern current_pattern=pattern_list.get(j);
						int step=write_positive_graphs_pattern_edge(current_pattern,start_edgeID);
						start_edgeID=start_edgeID+step;
					}
					write_positive_graphs_padding_and_remaining_edge(start_edgeID, false);  // false means second half
				}
				else
				{
					// generate random graphs
					for(int vertex=0;vertex<size_of_graphs;vertex++)		// output all the vertices
					{
						int vertex_label=(int)(Math.random()*max+1);
						out.println("v "+(vertex+1)+" "+vertex_label);
					}
					int edgeID=1;
					for(int vertex=0;vertex<size_of_graphs-1;vertex++)			// output all the edges (it is a chain here), you may want to make it complicated later
					{
						int edge_label=(int)(Math.random()*max+1);
						if(i==number_of_graphs-1 && vertex==size_of_graphs-2)
						{
							out.print("e "+(vertex+1)+" "+(vertex+2)+" "+edge_label+" "+edgeID);
						}
						else
						{
							out.println("e "+(vertex+1)+" "+(vertex+2)+" "+edge_label+" "+edgeID);	
						}
						edgeID++;
					}
				}
			}
			close();
		}catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	static void write_positive_graphs_pattern_vertex(pattern p, boolean variation, int start_vertexID)
	{
		p.start_vertex_ID=start_vertexID;
		p.end_vertex_ID=start_vertexID+size_of_pattern-1;
		for(int i=0;i<size_of_pattern;i++)
		{
			if(variation==true && (i==size_of_pattern-2 || i==size_of_pattern-3 || i==size_of_pattern-4))			// designed for one-error variation
			{	// up to three errors
				double random_number=Math.random();
				if(random_number<0.5)    // introduce some errors
				{
					int random_label=(int)(Math.random()*max+1);
					out.println("v "+start_vertexID+" "+random_label);
				}
				else					
				{
					out.println("v "+start_vertexID+" "+p.label[i][i]);
				}
			}
			else
			{
				out.println("v "+start_vertexID+" "+p.label[i][i]);
			}
			start_vertexID++;
		}
	}
	static void write_positive_graphs_remaining_vertex(int start_vertexID)
	{
		while(start_vertexID<=size_of_graphs)
		{
			int label=(int)(Math.random()*max+1);
			out.println("v "+start_vertexID+" "+label);
			start_vertexID++;
		}
	}
	static int write_positive_graphs_pattern_edge(pattern p, int start_edgeID)
	{
		int patternID=p.patternID%(number_of_pattern/2);
		int step=size_of_pattern*patternID;
		int number_of_edges=0;
		for(int i=0;i<size_of_pattern;i++)
		{
			for(int j=0;j<i;j++)
			{
				if(p.label[i][j]!=0)
				{
					out.println("e "+(p.ID[j][j]+step)+" "+(p.ID[i][i]+step)+" "+p.label[i][j]+" "+start_edgeID);
					start_edgeID++;
					number_of_edges++;
				}
			}
		}
		return number_of_edges;	
	}
	static void write_positive_graphs_padding_and_remaining_edge(int start_edgeID, boolean first_half)
	{
		int padding_edge_label;
		if(first_half==true)
		{
			for(int i=1;i<pattern_list.size()/2;i++)
			{
				// connect the start with the end
				int last_end=pattern_list.get(i-1).end_vertex_ID;
				int next_start=pattern_list.get(i).start_vertex_ID;
				padding_edge_label=(int)(Math.random()*max+1);
				out.println("e "+last_end+" "+padding_vertexID+" "+padding_edge_label+" "+start_edgeID);
				start_edgeID++;
				padding_edge_label=(int)(Math.random()*max+1);
				out.println("e "+padding_vertexID+" "+(padding_vertexID+1)+" "+padding_edge_label+" "+start_edgeID);
				padding_vertexID++;
				start_edgeID++;
				padding_edge_label=(int)(Math.random()*max+1);
				out.println("e "+padding_vertexID+" "+next_start+" "+padding_edge_label+" "+start_edgeID);
				padding_vertexID++;
				start_edgeID++;
				if(i==pattern_list.size()/2-1)
				{
					padding_edge_label=(int)(Math.random()*max+1);
					out.println("e "+pattern_list.get(i).end_vertex_ID+" "+(padding_vertexID)+" "+padding_edge_label+" "+start_edgeID);
					start_edgeID++;
					padding_vertexID++;
				}
			}
			while(padding_vertexID<=size_of_graphs)
			{
				// add edge
				int label=(int)(Math.random()*max+1);
				out.println("e "+(padding_vertexID-1)+" "+(padding_vertexID)+" "+label+" "+start_edgeID);
				padding_vertexID++;
				start_edgeID++;
			}
		}
		else
		{
			for(int i=pattern_list.size()/2+1;i<pattern_list.size();i++)
			{
				int last_end=pattern_list.get(i-1).end_vertex_ID;
				int next_start=pattern_list.get(i).start_vertex_ID;
				padding_edge_label=(int)(Math.random()*max+1);
				out.println("e "+last_end+" "+padding_vertexID+" "+padding_edge_label+" "+start_edgeID);
				start_edgeID++;
				padding_edge_label=(int)(Math.random()*max+1);
				out.println("e "+padding_vertexID+" "+(padding_vertexID+1)+" "+padding_edge_label+" "+start_edgeID);
				padding_vertexID++;
				start_edgeID++;
				padding_edge_label=(int)(Math.random()*max+1);
				out.println("e "+padding_vertexID+" "+next_start+" "+padding_edge_label+" "+start_edgeID);
				padding_vertexID++;
				start_edgeID++;
				if(i==pattern_list.size()-1)
				{
					padding_edge_label=(int)(Math.random()*max+1);
					out.println("e "+pattern_list.get(i).end_vertex_ID+" "+(padding_vertexID)+" "+padding_edge_label+" "+start_edgeID);
					start_edgeID++;
					padding_vertexID++;
				}
			}
			while(padding_vertexID<=size_of_graphs)
			{
				// add edge
				int label=(int)(Math.random()*max+1);
				out.println("e "+(padding_vertexID-1)+" "+(padding_vertexID)+" "+label+" "+start_edgeID);
				padding_vertexID++;
				start_edgeID++;
			}
		}
	}
	static void generate_negative_graphs()
	{
		try
		{
			fw = new FileWriter(negative_graph_address,false); 
			out = new PrintWriter(fw); 
			out.println(number_of_graphs);
			for(int i=0;i<number_of_graphs;i++)
			{
				out.println("t # "+i+" "+size_of_graphs);
				int current_vertexID=1;
				int current_edgeID=1;
				if(i<number_of_pattern)
				{
					pattern current_pattern=pattern_list.get(i);
					int[][] current_label_matrix=current_pattern.sub_label;
					for(int j=0;j<current_label_matrix.length;j++)				// write the vertex of the subgraph
					{
						out.println("v "+current_vertexID+" "+current_label_matrix[j][j]);
						current_vertexID++;
					}
					// write the remaining vertex
					while(current_vertexID<=size_of_graphs)
					{
						int vertex_label=(int)(Math.random()*max+1);
						out.println("v "+current_vertexID+" "+vertex_label);
						current_vertexID++;
					}
					for(int j=0;j<current_label_matrix.length;j++)				// write the vertex of the subgraph
					{
						for(int k=0;k<j;k++)
						{
							if(current_label_matrix[j][k]!=0)
							{
								out.println("e "+(k+1)+" "+(j+1)+" "+current_label_matrix[j][k]+" "+current_edgeID);
								current_edgeID++;
							}
						}
					}
					int last_subgraph_vertexID=current_label_matrix.length;
					int edge_label=(int)(Math.random()*max+1);
					out.println("e "+(last_subgraph_vertexID)+" "+(last_subgraph_vertexID+1)+" "+edge_label+" "+current_edgeID);		// write the padding edge
					current_edgeID++;
					// write the remaining edge
					for(int vertex=last_subgraph_vertexID;vertex<size_of_graphs-1;vertex++)			// output all the edges (it is a chain here), you may want to make it complicated later
					{
						edge_label=(int)(Math.random()*max+1);
						out.println("e "+(vertex+1)+" "+(vertex+2)+" "+edge_label+" "+current_edgeID);	
						current_edgeID++;
					}
				}
				else
				{
					for(int vertex=0;vertex<size_of_graphs;vertex++)		// output all the vertices
					{
						int vertex_label=(int)(Math.random()*max+1);
						out.println("v "+(vertex+1)+" "+vertex_label);
					}
					int edgeID=1;
					for(int vertex=0;vertex<size_of_graphs-1;vertex++)			// output all the edges (it is a chain here), you may want to make it complicated later
					{
						int edge_label=(int)(Math.random()*max+1);
						if(i==number_of_graphs-1 && vertex==size_of_graphs-2)
						{
							out.print("e "+(vertex+1)+" "+(vertex+2)+" "+edge_label+" "+edgeID);
						}
						else
						{
							out.println("e "+(vertex+1)+" "+(vertex+2)+" "+edge_label+" "+edgeID);	
						}
						edgeID++;
					}
				}
			}
			close();
		}catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	static void close()
	{
		out.flush();
		out.close();
		try 
		{
			fw.close();
		} 
		catch (IOException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

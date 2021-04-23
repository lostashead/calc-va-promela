package jacksongradle;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;

public class App {
	public static void main(String[] args) throws IOException {
		System.out.println("出力するモデルの数を入力");
		Scanner scanner = new Scanner(System.in);
		int scale = scanner.nextInt();
		scanner.close();
		for (int count = 0; count < scale; count++) {
			//結果を出力するためのファイルを作成する
			try {
				FileWriter file = new FileWriter("C:\\SPIN\\testmodel" + count + ".pml");
				PrintWriter writer = new PrintWriter(new BufferedWriter(file));
				//ファイルを読み込みpathオブジェクトを作る
				Path path = Paths.get("C:\\pleiades\\workspace\\jacksongradle\\files\\modeldata.json");

				//PathオブジェクトをListにする
				List stList = Files.readAllLines(path);

				//ListをStringに変換する
				String str = (String) stList.stream().collect(Collectors.joining(""));

				//mapperをインスタンス化
				ObjectMapper mapper = new ObjectMapper();
				Component[] component = mapper.readValue(str, Component[].class);

				//可変性部分を確率を用いて決定し、node配列に代入する
				Random random = new Random();
				String node[] = new String[component.length];
				String WithAlternative[] = new String[component.length];
				String OffOptional[] = new String[component.length];

				//出力でAfterConnectorにSortやRefindが表示されるのを防ぐために新しく
				for (int j = 0, i = 0; j < component.length; j++, i++) {

					//Optionalの確率が0でない場合採用するかしないかを決定する
					if (component[j].getOptional() != null) {
						//ランダム関数の出る数がyesのでる確率以下の場合、採用する
						if (random.nextDouble() < (double) component[j].getProbability().get(0)) {
							node[i] = (String) component[j].getState();
						} else {
							node[i] = null;
							OffOptional[i] = component[j].getState();
						}
					} else if (component[j].getAlternative() != null) {

						//WithAlternativeにAlternativeを持つStateを入力しておく(後からAfterConnectorのために使う)
						//ランダム関数の出る範囲によって要素を選択する
						double num = random.nextDouble();
						int PLength = component[j].getProbability().size();
						//Alternativeの数と確率を読み込んで乱数の出た範囲からAlternativeを決定する
						double p[] = new double[PLength + 1];
						double q[] = new double[PLength + 1];
						p[0] = 0;
						q[0] = 0;
						for (int x = 1; x <= PLength; x++) {
							p[x] = (double) component[j].getProbability().get(x - 1);
							q[x] = q[x - 1] + p[x];
							if (q[x - 1] <= num && num < q[x]) {
								node[i] = (String) component[j].getAlternative().get(x - 1);
								WithAlternative[i] = component[j].getState();
							}
						}
					} else {
						node[i] = component[j].getState();
					}
				}

				//決定した可変性部分を使用して新しいAfterConnectorを作成する
				String NewAC[][] = new String[component.length][component.length];
				for (int k = 0; k < component.length; k++) {
					if (component[k].getAfterConnector() != null) {
						for (int l = 0; l < component[k].getAfterConnector().size(); l++) {
							if (Arrays.asList(WithAlternative).contains(component[k].getAfterConnector().get(l))) {
								for (int m = 0; m < component.length; m++) {
									if (component[k].getAfterConnector().get(l).equals(WithAlternative[m])) {
										NewAC[k][l] = node[m];
									}
								}
							} else if (Arrays.asList(OffOptional).contains(component[k].getAfterConnector().get(l))) {
								continue;
							} else {
								NewAC[k][l] = (String) component[k].getAfterConnector().get(l);
							}
						}
					}
				}

				//出力する（promela形式）
				//ステートマシンのための定数、変数の宣言mtype = {};
				//writerメソッドはファイル出力→見にくいが、処理が一回で済むようにsystem.out.printに続けて書いた
				System.out.print("mtype = {");
				writer.print("mtype = {");
				for (int a = 0; a < component.length; a++) {
					if (node[a] != null) {
						System.out.print(node[a]);
						writer.print(node[a]);
						if (a == component.length - 1) {
							break;
						}
						System.out.print(", ");
						writer.print(", ");
					}
				}
				System.out.println("};");
				writer.println("};");

				//各ステートマシンの状態変数
				System.out.print("mtype state = ");
				System.out.print(node[0]);
				System.out.println(";");
				//各プロセスのイベント受信用通信チャネル
				System.out.println();
				writer.print("mtype state = ");
				writer.print(node[0]);
				writer.println(";");
				writer.println();;

				//ここからプロセスの記述
				//各ステートマシンの定義
				System.out.println();
				System.out.println();
				System.out.println("active proctype Statemachine(){");
				System.out.println();
				System.out.println();
				System.out.println("do");

				//ステートマシンが複数の場合を考える？？？→解決
				System.out.println(":: if");
				writer.println();
				writer.println();
				writer.println("active proctype Statemachine(){");
				writer.println();
				writer.println();
				writer.println("do");
				writer.println(":: if");

				//ループ？
				for (int d = 0; d < component.length; d++) {
					if (node[d] != null) {
						if (component[d].getAfterConnector() != null) {
							for (int e = 0; e < component[d].getAfterConnector().size(); e++) {
								if (NewAC[d][e] != null) {
									System.out.print(":: state==");
									System.out.print(node[d]);
									System.out.print(" -> state=");
									System.out.print(NewAC[d][e]);
									System.out.println();

									writer.print(":: state==");
									writer.print(node[d]);
									writer.print(" -> state=");
									writer.print(NewAC[d][e]);
									writer.println();
								} else {
									System.out.print(":: state==");
									System.out.print(node[d]);
									System.out.print(" -> skip");
									System.out.println();

									writer.print(":: state==");
									writer.print(node[d]);
									writer.print(" -> skip");
									writer.println();
								}
							}
						} else {
							System.out.print(":: state==");
							System.out.print(node[d]);
							System.out.print(" -> skip");
							System.out.println();

							writer.print(":: state==");
							writer.print(node[d]);
							writer.print(" -> skip");
							writer.println();
						}
					}
				}
				System.out.println("fi");
				System.out.println("od");
				System.out.println("}");
				System.out.println();
				System.out.println();
				System.out.println();

				writer.println("fi");
				writer.println("od");
				writer.println("}");
				writer.println();
				writer.println();
				writer.println();

				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
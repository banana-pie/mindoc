package com.me.mindoc;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.nutz.dao.Dao;
import org.nutz.dao.Sqls;
import org.nutz.dao.impl.NutDao;
import org.nutz.dao.impl.SimpleDataSource;
import org.nutz.dao.sql.Sql;

import com.me.mindoc.database.BaseFactory;
import com.me.mindoc.database.Database;
import com.me.mindoc.domain.ColumnVo;
import com.me.mindoc.domain.TableVo;

import freemarker.template.Configuration;
import freemarker.template.Template;

/**
 * 生成
 * @author dwl
 *
 */
public class Generator {
	
	private String url;
	
	private String user;
	
	private String password;
	
	private String name;
	
	private String type;

	public Generator(String url, String user, String password, String name, String type) {
		super();
		this.url = url;
		this.user = user;
		this.password = password;
		this.name = name;
		this.type = type;
	}
	
	public String makeDoc(){
		try {
			Database database = BaseFactory.getDataBase(this.type, this.name);
			SimpleDataSource dataSource = new SimpleDataSource();
			dataSource.setJdbcUrl(url);
	        dataSource.setUsername(user);
	        dataSource.setPassword(password);
	        dataSource.setDriverClassName(database.getType());
	        Dao dao = new NutDao(dataSource);
	        
	        Sql tableSql = Sqls.create(database.getTablesSql(database));
	        tableSql.setCallback(Sqls.callback.entities());
	        tableSql.setEntity(dao.getEntity(TableVo.class));
	        dao.execute(tableSql);
	        List<TableVo> list = tableSql.getList(TableVo.class);
	        
	        
	        for(TableVo table :list ){
	        	Sql columnSql = Sqls.create(database.getColumnSql(database, table.getTableName()));
	        	columnSql.setCallback(Sqls.callback.entities());
	        	columnSql.setEntity(dao.getEntity(ColumnVo.class));
		        dao.execute(columnSql);
		        table.setColumns(columnSql.getList(ColumnVo.class));
	        }
	        dataSource.close();
	        return createDoc(list);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "导出失败！请检查配置。";
		
		
		
		
	}
	
	
	 public String createDoc(List<TableVo> tables){
	        // 第一步：创建一个Configuration对象。
	        Configuration configuration = new Configuration(Configuration.getVersion());
	        // 第二步：设置模板文件所在的路径。
	        try {
	            configuration.setClassForTemplateLoading(this.getClass(), "template");
	            // 第三步：设置模板文件使用的字符集。
	            configuration.setEncoding(Locale.getDefault(), "utf-8");
	            // 第四步：加载一个模板，创建一个模板对象。
	            Template template = configuration.getTemplate("doc.xml");
	            // 第五步：创建一个模板使用的数据集。
	            Map<String,Object> dataModel = new HashMap<String, Object>();
	            dataModel.put("tables",tables);
	            //生成文件放在项目根目录下
	            File outFile = new File(System.getProperty("user.dir")+"\\DatabaseDesign.doc");
	            Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outFile),"UTF-8"));
	            // 第六步：调用模板对象的process方法输出文件。
	            template.process(dataModel, out);
	            return "文件路径-"+System.getProperty("user.dir")+"\\DatabaseDesign.doc";
	        } catch (Exception e) {
	        	e.printStackTrace();
	        }
			return "导出失败！请检查配置。";
	    }

}

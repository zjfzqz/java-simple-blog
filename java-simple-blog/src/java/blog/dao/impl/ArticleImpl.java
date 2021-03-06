/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package blog.dao.impl;

import blog.system.dao.AbstractDaoImpl;
import blog.entity.Article;
import blog.entity.Category;
import blog.entity.Content;
import blog.system.dao.DaoFactory;
import blog.system.exception.PersistException;
import blog.system.loader.Load;
import blog.system.tools.Logger;
import static java.net.Proxy.Type.HTTP;
import java.net.URLEncoder;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author petroff
 */
public class ArticleImpl extends AbstractDaoImpl<Article> {

    @Override
    public String queryFindAll() throws PersistException {
        return "SELECT * FROM blogj.article;";
    }

    @Override
    public void prepareQuery(PreparedStatement statement, int pid) throws PersistException {
        try {
            statement.setInt(1, pid);
            statement.setInt(2, Load.auth.getUserId());
        } catch (Exception e) {
            throw new PersistException(e);
        }
    }

    @Override
    public void prepareQuery(PreparedStatement statement, Article a) throws PersistException {
        try {
            statement.setBoolean(1, a.isEnable());
            statement.setString(2, a.getAlias());
            statement.setInt(3, a.getWeight());

            statement.setInt(4, Load.auth.getUserId());
            statement.setInt(5, a.getCategory_id());
        } catch (Exception e) {
            throw new PersistException(e);
        }
    }

    @Override
    public List<Article> parseResultSet(ResultSet rs) throws PersistException {
        List<Article> listArticles = new ArrayList();
        Article article = new Article();
        try {
            rs.next();

            listArticles.add(article);
        } catch (Exception e) {
            throw new PersistException(e);
        }
        return listArticles;
    }

    @Override
    public String queryFindByPk() throws PersistException {
        return "SELECT * FROM blogj.article WHERE id = ?";
    }

    @Override
    public String queryUpdate() throws PersistException {
        return "UPDATE blogj.article SET enable = ?, alias = ?, weight = ?, category_id = ? WHERE id = ? AND user_id = ?";
    }

    @Override
    public String queryInsert() throws PersistException {
        return "INSERT blogj.article (enable, alias, weight, user_id, ut, category_id) VALUE(?, ?, ?, ?, NOW(), ?);";
    }

    @Override
    public String queryDelete() throws PersistException {
        return "DELETE c, con FROM blogj.article c inner join blogj.content con ON c.id = con.object_id and "
                + "(con.`type` = 'article_t' or con.`type` = 'article_b')  WHERE c.id = ? and c.user_id = ?;";
    }

    @Override
    public void prepareQueryUpdate(PreparedStatement statement, Article entity) throws PersistException {
        try {
            statement.setBoolean(1, entity.isEnable());
            statement.setString(2, entity.getAlias());
            statement.setInt(3, entity.getWeight());
            statement.setInt(4, entity.getCategory_id());
            statement.setInt(5, entity.getId());
            statement.setInt(6, Load.auth.getUserId());
        } catch (Exception e) {
            throw new PersistException(e);
        }
    }

    @Override
    public Integer insert(Article article) throws PersistException {
        Integer res;
        res = super.insert(article);
        try {
            if (res != null) {
                article.setId(res);
                if (!insertContent(article)) {
                    throw new PersistException("Can't insert content");
                }
            }
        } catch (PersistException p) {
            super.rollbackTransaction();
            throw p;
        }
        return res;
    }

    public boolean insertContent(Article article) throws PersistException {
        ContentImpl contentImpl = (ContentImpl) DaoFactory.getDao("ContentImpl");

        for (Map.Entry<String, String> entry : article.translate_title.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            Content content = new Content();
            content.setType(article.getType() + "_t");
            content.setObject_id(article.getId());
            content.setUser_id(Load.auth.getUserId());
            content.setLang(key);
            content.setText(value);
            Integer res = contentImpl.insert(content);
            if (res == null) {
                throw new PersistException("Can't insert content");
            }
        }

        for (Map.Entry<String, String> entry : article.translate_body.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            Content content = new Content();
            content.setType(article.getType() + "_b");
            content.setObject_id(article.getId());
            content.setUser_id(Load.auth.getUserId());
            content.setLang(key);
            content.setText(value);
            Integer res = contentImpl.insert(content);
            if (res == null) {
                throw new PersistException("Can't insert content");
            }
        }

        return true;
    }

    @Override
    public Article findByPk(int article_id) throws PersistException {
        Article article = new Article();
        String sql = "select a.*, co.* from blogj.article a inner join blogj.content co "
                + "ON a.id = co.object_id and (co.`type` = 'article_t' or co.`type` = 'article_b') and a.id = ? and a.user_id = ?;";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, article_id);
            statement.setInt(2, Load.auth.getUserId());
            ResultSet rs = statement.executeQuery();

            while (rs.next()) {
                article.setId(rs.getInt("id"));
                article.setEnable(rs.getBoolean("enable"));
                article.setAlias(rs.getString("alias"));
                article.setWeight(rs.getInt("weight"));
                article.setCategory_id(rs.getInt("category_id"));
                article.setUser_id(rs.getInt("user_id"));
                String lang = rs.getString("lang");
                String text = rs.getString("text");
                if (rs.getString("type").equals("article_t")) {
                    article.translate_title.put(lang, text);
                } else {
                    article.translate_body.put(lang, text);
                }
            }
            rs.close();
        } catch (Exception e) {
            throw new PersistException(e);
        }
        return article;
    }

    @Override
    public boolean update(Article article) throws PersistException {
        boolean res;
        res = super.update(article);
        try {
            if (res) {
                if (!updateContent(article)) {
                    throw new PersistException("Can't update content");
                }
            }
        } catch (PersistException p) {
            super.rollbackTransaction();
            throw p;
        }
        return res;
    }

    public boolean updateContent(Article article) throws PersistException {
        ContentImpl contentImpl = (ContentImpl) DaoFactory.getDao("ContentImpl");

        for (Map.Entry<String, String> entry : article.translate_title.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            Content content = new Content();
            content.setType(article.getType() + "_t");
            content.setObject_id(article.getId());
            content.setUser_id(Load.auth.getUserId());
            content.setLang(key);
            content.setText(value);
            boolean res = contentImpl.update(content);
            if (!res) {
                throw new PersistException("Can't update content");
            }
        }

        for (Map.Entry<String, String> entry : article.translate_body.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            Content content = new Content();
            content.setType(article.getType() + "_b");
            content.setObject_id(article.getId());
            content.setUser_id(Load.auth.getUserId());
            content.setLang(key);
            content.setText(value);
            boolean res = contentImpl.update(content);
            if (!res) {
                throw new PersistException("Can't update content");
            }
        }

        return true;
    }

    public int findByAlias(String alias, int article_id) throws PersistException {
        int count = 0;
        String sql = "SELECT * FROM blogj.article t  WHERE t.alias = ? AND t.user_id = ? and t.id != ?;";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, alias);
            statement.setInt(2, Load.auth.getUserId());
            statement.setInt(3, article_id);
            ResultSet rs = statement.executeQuery();
            rs.last();
            count = rs.getRow();
            rs.close();
        } catch (Exception e) {
            throw new PersistException(e);
        }
        return count;
    }

    public List<Article> findAllForUser(int userId) throws PersistException {

        List<Article> articles = new ArrayList();
        String sql = "SELECT t.*, title.text as title, body.text as body, title.lang as lang FROM blogj.article t  "
                + "INNER JOIN blogj.content title "
                + "ON t.id = title.object_id AND title.`type` = 'article_t' and title.lang = ? "
                + "INNER JOIN blogj.content body "
                + "ON t.id = body.object_id AND body.`type` = 'article_b' and body.lang = ? "
                + "WHERE  t.user_id = ?  GROUP BY t.id;";
        //////Logger.write(sql);
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, Load.lang.get());
            statement.setString(2, Load.lang.get());
            statement.setInt(3, userId);
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                Article article = new Article();
                article.setId(rs.getInt("id"));
                article.setEnable(rs.getBoolean("enable"));
                article.setAlias(rs.getString("alias"));
                article.setWeight(rs.getInt("weight"));
                article.setUser_id(rs.getInt("user_id"));
                String lang = rs.getString("lang");
                String title = rs.getString("title");
                String body = rs.getString("body");
                article.translate_title.put(lang, title);
                article.translate_body.put(lang, body);
                articles.add(article);
            }
            rs.close();
        } catch (Exception e) {
            throw new PersistException(e);
        }
        return articles;
    }

    public List<Article> findAllForMain(Integer category_id) throws PersistException {

        List<Article> articles = new ArrayList();
        String sql;
        if (category_id == null) {
            sql = "SELECT t.*, title.text as title, body.text as body, title.lang as lang, u.user_name FROM blogj.article t  "
                    + "INNER JOIN blogj.content title "
                    + "ON t.id = title.object_id AND title.`type` = 'article_t' and title.lang = ? "
                    + "INNER JOIN blogj.content body "
                    + "ON t.id = body.object_id AND body.`type` = 'article_b' and body.lang = ? "
                    + "INNER JOIN (SELECT * FROM users ut LIMIT 10) u "
                    + "ON u.id = t.user_id "
                    + "WHERE t.enable = true GROUP BY t.id;";
        } else {
            sql = "SELECT t.*, title.text as title, body.text as body, title.lang as lang, u.user_name FROM blogj.article t  "
                    + "INNER JOIN blogj.content title "
                    + "ON t.id = title.object_id AND title.`type` = 'article_t' and title.lang = ? "
                    + "INNER JOIN blogj.content body "
                    + "ON t.id = body.object_id AND body.`type` = 'article_b' and body.lang = ? "
                    + "INNER JOIN (SELECT * FROM users ut LIMIT 10) u "
                    + "ON u.id = t.user_id "
                    + "WHERE t.enable = true AND category_id = ? GROUP BY t.id;";
        }
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            if (category_id == null) {
                statement.setString(1, Load.lang.get());
                statement.setString(2, Load.lang.get());
            } else {
                statement.setString(1, Load.lang.get());
                statement.setString(2, Load.lang.get());
                statement.setInt(3, category_id);
            }
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                Article article = new Article();
                article.setId(rs.getInt("id"));
                article.setEnable(rs.getBoolean("enable"));
                article.setAlias(rs.getString("alias"));
                article.setWeight(rs.getInt("weight"));
                String lang = rs.getString("lang");
                String title = rs.getString("title");
                String body = rs.getString("body");
                article.setUserName(rs.getString("user_name"));
                article.translate_title.put(lang, title);

                article.translate_body.put(lang, body);
                article.setUt(rs.getString("ut"));
                article.setUser_id(rs.getInt("user_id"));
                articles.add(article);
            }
            rs.close();
        } catch (Exception e) {
            throw new PersistException(e);
        }
        return articles;
    }

    public Article findByAliasUser(int user_id, String article_alias) throws PersistException {
        Article article = new Article();
        String sql = "select a.*, co.*, u.user_name from blogj.article a inner join blogj.content co "
                + "ON a.id = co.object_id and (co.`type` = 'article_t' or co.`type` = 'article_b') and a.alias = ? and a.user_id = ? and a.enable=true "
                + "inner join users u on u.id = a.user_id;";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, article_alias);
            statement.setInt(2, user_id);
            ResultSet rs = statement.executeQuery();

            while (rs.next()) {
                article.setId(rs.getInt("id"));
                article.setEnable(rs.getBoolean("enable"));
                article.setAlias(rs.getString("alias"));
                article.setWeight(rs.getInt("weight"));
                article.setCategory_id(rs.getInt("category_id"));
                article.setUser_id(rs.getInt("user_id"));
                String lang = rs.getString("lang");
                String text = rs.getString("text");
                article.setUserName(rs.getString("user_name"));
                article.setUt(rs.getString("ut"));
                if (rs.getString("type").equals("article_t")) {
                    article.translate_title.put(lang, text);
                } else {
                    article.translate_body.put(lang, text);
                }
            }
            rs.close();
        } catch (Exception e) {
            throw new PersistException(e);
        }
        return article;
    }

    public List<Article> findAllForCategory(Integer category_id) throws PersistException {

        List<Article> articles = new ArrayList();
        String sql;

        sql = "SELECT t.*, title.text as title, body.text as body, title.lang as lang, u.user_name FROM blogj.article t  "
                + "INNER JOIN blogj.content title "
                + "ON t.id = title.object_id AND title.`type` = 'article_t' and title.lang = ? "
                + "INNER JOIN blogj.content body "
                + "ON t.id = body.object_id AND body.`type` = 'article_b' and body.lang = ? "
                + "INNER JOIN   users  u "
                + "ON u.id = t.user_id "
                + "WHERE t.enable = true AND category_id = ? GROUP BY t.id;";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            if (category_id == null) {
                statement.setString(1, Load.lang.get());
                statement.setString(2, Load.lang.get());
            } else {
                statement.setString(1, Load.lang.get());
                statement.setString(2, Load.lang.get());
                statement.setInt(3, category_id);
            }
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                Article article = new Article();
                article.setId(rs.getInt("id"));
                article.setEnable(rs.getBoolean("enable"));
                article.setAlias(rs.getString("alias"));
                article.setWeight(rs.getInt("weight"));
                String lang = rs.getString("lang");
                String title = rs.getString("title");
                String body = rs.getString("body");
                article.setUserName(rs.getString("user_name"));
                article.translate_title.put(lang, title);

                article.translate_body.put(lang, body);
                article.setUt(rs.getString("ut"));
                article.setUser_id(rs.getInt("user_id"));
                articles.add(article);
            }
            rs.close();
        } catch (Exception e) {
            throw new PersistException(e);
        }
        return articles;
    }

    public List<Article> findAllCustom(int page, String search) throws PersistException {

        List<Article> articles = new ArrayList();
        String sql;
        sql = "SELECT t.*, title.text as title, body.text as body, title.lang as lang, u.user_name FROM blogj.article t  "
                + "INNER JOIN blogj.content title "
                + "ON t.id = title.object_id AND title.`type` = 'article_t' and title.lang = ? "
                + "INNER JOIN blogj.content body "
                + "ON t.id = body.object_id AND body.`type` = 'article_b' and body.lang = ? "
                + "INNER JOIN   users  u "
                + "ON u.id = t.user_id "
                + "WHERE t.enable = true  ";

        if (!search.isEmpty()) {
            sql = sql + " AND (body.text LIKE ? OR title.text LIKE ?) ";
        }

        sql = sql + " GROUP BY t.id ORDER BY t.id DESC LIMIT ? OFFSET ?;";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, Load.lang.get());
            statement.setString(2, Load.lang.get());

            if (!search.isEmpty()) {

                statement.setString(3, "%" + search + "%");
                statement.setString(4, "%" + search + "%");
                statement.setInt(5, Load.config.limit);
                statement.setInt(6, (page - 1) * Load.config.limit);
            } else {
                statement.setInt(3, Load.config.limit);
                statement.setInt(4, (page - 1) * Load.config.limit);
            }

            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                Article article = new Article();
                article.setId(rs.getInt("id"));
                article.setEnable(rs.getBoolean("enable"));
                article.setAlias(rs.getString("alias"));
                article.setWeight(rs.getInt("weight"));
                String lang = rs.getString("lang");
                String title = rs.getString("title");
                String body = rs.getString("body");
                article.setUserName(rs.getString("user_name"));
                article.translate_title.put(lang, title);

                article.translate_body.put(lang, body);
                article.setUt(rs.getString("ut"));
                article.setUser_id(rs.getInt("user_id"));
                articles.add(article);
            }
            rs.close();
        } catch (Exception e) {
            throw new PersistException(e);
        }
        return articles;
    }

    public int count(String search) throws PersistException {

        int count = 0;
        String sql = "SELECT count(t.id) as count FROM blogj.article t  "
                + "INNER JOIN blogj.content title "
                + "ON t.id = title.object_id AND title.`type` = 'article_t' and title.lang = ? "
                + "INNER JOIN blogj.content body "
                + "ON t.id = body.object_id AND body.`type` = 'article_b' and body.lang = ? "
                + "WHERE t.enable = true  ";
        if (!search.isEmpty()) {
            sql = sql + " AND (body.text LIKE ? OR title.text LIKE ?) ";
        }
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, Load.lang.get());
            statement.setString(2, Load.lang.get());
            if (!search.isEmpty()) {
                statement.setString(3, "%" + search + "%");
                statement.setString(4, "%" + search + "%");
            }
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                count = rs.getInt("count");
            }
            rs.close();
        } catch (Exception e) {
            throw new PersistException(e);
        }
        return count;
    }

}

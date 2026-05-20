<template>
  <nav class="bid-rail-sidebar" aria-label="主导航">
    <div class="rail-scroll">
      <template v-for="item in railItems">
        <el-popover
          v-if="item.children && item.children.length"
          :key="item.key"
          popper-class="rail-menu-popper"
          placement="right-start"
          trigger="hover"
          :visible-arrow="false"
        >
          <div class="rail-popover-panel">
            <div class="rail-popover-title">{{ item.title }}</div>
            <button
              v-for="child in item.children"
              :key="child.key"
              type="button"
              class="rail-popover-item"
              :class="{ active: isActive(child) }"
              @click="handleNavigate(child)"
            >
              <i :class="getIconClass(child)" />
              <span>{{ child.title }}</span>
            </button>
          </div>
          <button
            slot="reference"
            type="button"
            class="rail-item"
            :class="{ active: isActive(item) }"
            :title="item.title"
            @click="handleNavigate(item)"
          >
            <i :class="getIconClass(item)" />
            <span>{{ item.title }}</span>
          </button>
        </el-popover>

        <button
          v-else
          :key="item.key"
          type="button"
          class="rail-item"
          :class="{ active: isActive(item), 'has-badge': item.badge }"
          :title="item.title"
          @click="handleNavigate(item)"
        >
          <i :class="getIconClass(item)" />
          <span>{{ item.title }}</span>
          <em v-if="item.badge">{{ item.badge }}</em>
        </button>
      </template>
    </div>
  </nav>
</template>

<script>
import path from 'path'
import { mapGetters } from 'vuex'
import { isExternal } from '@/utils/validate'

const ICON_MAP = {
  首页: 'el-icon-s-home',
  AI方案: 'el-icon-magic-stick',
  AI标书: 'el-icon-document',
  AI质检: 'el-icon-circle-check',
  方案查重: 'el-icon-refresh',
  知识库: 'el-icon-collection',
  私人图库: 'el-icon-picture-outline',
  资料库: 'el-icon-folder-opened',
  产品库: 'el-icon-box',
  标讯商机: 'el-icon-s-opportunity',
  常用网站: 'el-icon-link',
  AI工具: 'el-icon-cpu',
  下载中心: 'el-icon-download',
  系统管理: 'el-icon-setting',
  系统监控: 'el-icon-monitor',
  系统工具: 'el-icon-suitcase',
  回收站: 'el-icon-delete'
}

const TARGET_PATH_MAP = {
  AI方案: '/bid/plan'
}

const ACTIVE_ALIAS_MAP = {
  '/bid/ai-plan': ['/bid/plan'],
  '/bid/plan': ['/bid/ai-plan'],
  '/bid/gallery': ['/bid/tool/gallery'],
  '/bid/tool': ['/bid/tool/gallery']
}

export default {
  name: 'Sidebar',
  computed: {
    ...mapGetters(['sidebarRouters']),
    railItems() {
      const routes = this.sidebarRouters || []
      const items = []

      routes.forEach(route => {
        if (!route || route.hidden) {
          return
        }

        const title = route.meta && route.meta.title
        if (route.path === '' || title === '首页') {
          const home = this.singleVisibleChild(route)
          if (home) {
            items.push(this.toRailItem(home, route.path))
          }
          return
        }

        if (route.path === '/bid' || title === '标书智能写作') {
          this.visibleChildren(route).forEach(child => {
            items.push(this.toRailItem(child, route.path))
          })
          return
        }

        if (['系统管理', '系统监控', '系统工具'].includes(title)) {
          items.push(this.toRailItem(route, ''))
        }
      })

      return items
    }
  },
  methods: {
    visibleChildren(route) {
      return (route.children || []).filter(child => child && !child.hidden && child.path !== '#')
    },
    singleVisibleChild(route) {
      const children = this.visibleChildren(route)
      return children.length ? children[0] : route
    },
    toRailItem(route, basePath) {
      const title = (route.meta && route.meta.title) || route.name || ''
      const fullPath = TARGET_PATH_MAP[title] || this.resolvePath(basePath, route.path || '')
      const childBasePath = this.resolvePath(basePath, route.path || '')
      const children = this.flattenChildren(this.visibleChildren(route), childBasePath)

      return {
        key: `${title}-${fullPath}`,
        title,
        icon: route.meta && route.meta.icon,
        path: fullPath,
        matchPaths: this.getMatchPaths(fullPath, title, children),
        badge: title === '产品库' ? 'new' : '',
        children
      }
    },
    flattenChildren(children, basePath) {
      const result = []
      children.forEach(child => {
        const title = (child.meta && child.meta.title) || child.name || ''
        if (!title) {
          return
        }
        const childPath = TARGET_PATH_MAP[title] || this.resolvePath(basePath, child.path || '')
        const nested = this.visibleChildren(child)
        if (nested.length) {
          result.push({
            key: `${title}-${childPath}`,
            title,
            icon: child.meta && child.meta.icon,
            path: childPath,
            matchPaths: this.getMatchPaths(childPath, title, []),
            children: this.flattenChildren(nested, childPath)
          })
        } else {
          result.push({
            key: `${title}-${childPath}`,
            title,
            icon: child.meta && child.meta.icon,
            path: childPath,
            matchPaths: this.getMatchPaths(childPath, title, [])
          })
        }
      })
      return result
    },
    getMatchPaths(fullPath, title, children) {
      const paths = [fullPath].concat(ACTIVE_ALIAS_MAP[fullPath] || [])
      if (title === 'AI方案') {
        paths.push('/bid/plan')
      }
      children.forEach(child => {
        paths.push(child.path)
        ;(child.matchPaths || []).forEach(item => paths.push(item))
      })
      return Array.from(new Set(paths.filter(Boolean)))
    },
    getIconClass(item) {
      return ICON_MAP[item.title] || this.iconFallback(item.icon)
    },
    iconFallback(icon) {
      const fallbackMap = {
        dashboard: 'el-icon-s-home',
        edit: 'el-icon-edit-outline',
        form: 'el-icon-document',
        validCode: 'el-icon-circle-check',
        search: 'el-icon-search',
        education: 'el-icon-collection',
        picture: 'el-icon-picture-outline',
        tool: 'el-icon-cpu',
        download: 'el-icon-download',
        system: 'el-icon-setting',
        monitor: 'el-icon-monitor',
        build: 'el-icon-suitcase'
      }
      return fallbackMap[icon] || 'el-icon-menu'
    },
    resolvePath(basePath, routePath) {
      if (isExternal(routePath)) {
        return routePath
      }
      if (isExternal(basePath)) {
        return basePath
      }
      return path.resolve(basePath || '/', routePath || '')
    },
    isActive(item) {
      const currentPath = this.$route.meta.activeMenu || this.$route.path
      return (item.matchPaths || [item.path]).some(menuPath => {
        if (!menuPath || isExternal(menuPath)) {
          return false
        }
        return currentPath === menuPath || currentPath.indexOf(`${menuPath}/`) === 0
      })
    },
    firstNavigableChild(item) {
      if (!item.children || !item.children.length) {
        return null
      }
      for (const child of item.children) {
        if (child.children && child.children.length) {
          const nested = this.firstNavigableChild(child)
          if (nested) {
            return nested
          }
        }
        if (child.path) {
          return child
        }
      }
      return null
    },
    handleNavigate(item) {
      const target = item.children && item.children.length ? this.firstNavigableChild(item) || item : item
      if (!target || !target.path) {
        return
      }
      if (isExternal(target.path)) {
        window.open(target.path, '_blank')
      } else {
        this.$router.push(target.path)
      }
    }
  }
}
</script>

<style lang="scss">
.bid-rail-sidebar {
  height: 100%;
  background: #f7f9ff;
  border-right: 1px solid #e9edf5;
}

.rail-scroll {
  height: 100%;
  padding: 8px 0;
  overflow-y: auto;
  overflow-x: hidden;
}

.rail-scroll::-webkit-scrollbar {
  width: 0;
}

.rail-item {
  position: relative;
  display: flex;
  width: 64px;
  min-height: 54px;
  margin: 0 auto 2px;
  padding: 6px 2px 5px;
  border: 0;
  border-radius: 0;
  background: transparent;
  color: #2f3542;
  cursor: pointer;
  align-items: center;
  justify-content: center;
  flex-direction: column;
  gap: 4px;
  font-size: 12px;
  line-height: 1.15;
  transition: color .18s ease, background .18s ease;
}

.rail-item i {
  font-size: 20px;
  line-height: 1;
}

.rail-item span {
  width: 100%;
  word-break: keep-all;
  white-space: normal;
  text-align: center;
}

.rail-item:hover,
.rail-item.active {
  color: #2f6bff;
  background: #ffffff;
}

.rail-item.active::before {
  content: "";
  position: absolute;
  left: 0;
  top: 10px;
  bottom: 10px;
  width: 3px;
  border-radius: 0 3px 3px 0;
  background: #2f6bff;
}

.rail-item em {
  position: absolute;
  top: 4px;
  right: 4px;
  padding: 0 3px;
  border-radius: 6px;
  background: #ff4d4f;
  color: #fff;
  font-size: 9px;
  font-style: normal;
  line-height: 12px;
}

.rail-menu-popper {
  min-width: 156px;
  margin-left: 6px !important;
  padding: 8px;
  border: 1px solid #e8edf7;
  border-radius: 8px;
  box-shadow: 0 12px 32px rgba(39, 56, 96, .14);
}

.rail-popover-title {
  padding: 6px 8px 8px;
  color: #1f2937;
  font-size: 13px;
  font-weight: 700;
}

.rail-popover-item {
  display: flex;
  width: 100%;
  height: 36px;
  padding: 0 10px;
  border: 0;
  border-radius: 6px;
  background: transparent;
  color: #4b5563;
  cursor: pointer;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  text-align: left;
}

.rail-popover-item:hover,
.rail-popover-item.active {
  background: #f2f6ff;
  color: #2f6bff;
}

.rail-popover-item i {
  font-size: 16px;
}
</style>
